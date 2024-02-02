package com.yupi.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.usercenter.common.ErrorCode;
import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.service.UserService;
import com.yupi.usercenter.model.domain.User;
import com.yupi.usercenter.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.yupi.usercenter.contant.UserConstant.USER_LOGIN_STATE;

/**
* @author 86133
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2024-01-10 12:05:37
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "yupi";

    /**
     * 用户注册业务逻辑
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 新用户的id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword,String planetCode) {
        // 1.1.校验,引入了Java工具了commons-lang3,使用工具类里的isAnyBlank()方法就相当于校验了所传参数判空等操作
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if(userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号过短");
        }
        if(userPassword.length() < 8 || checkPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");
        }
        if (planetCode.length() > 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编号过长");
        }

        // 1.3.账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号包含特殊字符");
        }
        // 1.4.密码和校验密码相同
        if (!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码和校验密码不相同");
        }
        // 1.2.账户不能重复(放在后面可以省去一次查询数据库的资源调用)
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if(count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号重复");
        }
        // 1.3 星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if(count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"编号重复");
        }

        // 2.1.加密密码
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 3.1.向用户数据库中插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if(!saveResult){
            // 因为前面是Long类型，后面写的时候用的是long,所以当结果为null时会出现拆箱错误，因此将null换为-1
            return -1;
        }

        return user.getId();
    }

    /**
     * 用户登陆业务逻辑
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param request 浏览器请求(此处给请求的session设置值，从请求的session中获取值)
     * @return 用户脱敏信息
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1.1.校验,引入了Java工具了commons-lang3,使用工具类里的isAnyBlank()方法就相当于校验了所传参数判空等操作
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            // todo 修改为自定义异常
            return null;
        }
        if(userAccount.length() < 4){
            return null;
        }
        if(userPassword.length() < 8){
            return null;
        }

        // 1.3.账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()){
            return null;
        }

        // 2.1.加密密码
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 2.2.查询数据库（查询用户是否存在）
        // todo 逻辑删除的是否需要查询 ok
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 2.3.用户不存在
        if (user == null) {
            log.info("User login failed,userAccount cannot match userPassword");
            return null;
        }
        // 4.1.先进行用户脱敏(要返回给前端什么我们就指定什么，防止数据库的字段泄露给前端)
        User safetyUser = getSafetyUser(user);

        // 3.1.再记录用户的[登录态]
        request.getSession().setAttribute(USER_LOGIN_STATE,safetyUser);
        return safetyUser;
    }

    /**
     * 抽离出来的用户脱敏的方法
     * @param originUser 要处理的user
     * @return 要返回给前端什么我们就指定什么，防止数据库的字段泄露给前端
     */
    @Override
    public User getSafetyUser(User originUser){
        if (originUser == null){
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserStatus(0);
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUserRole(originUser.getUserRole());
        return safetyUser;
    }

    /**
     * 请求用户注销
     * @param request 请求对象
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }
}




