package com.yupi.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.usercenter.common.BaseResponse;
import com.yupi.usercenter.common.ErrorCode;
import com.yupi.usercenter.common.ResultUtils;
import com.yupi.usercenter.contant.UserConstant;
import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.model.domain.User;
import com.yupi.usercenter.model.domain.request.UserLoginRequest;
import com.yupi.usercenter.model.domain.request.UserRegisterRequest;
import com.yupi.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.yupi.usercenter.contant.UserConstant.ADMIN_ROLE;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    // 用户注册
    // @RequestBody 为了让springmvc框架识别前端传来的json参数去和此对象做关联
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if(userRegisterRequest == null){
//            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        // controller层不包含业务逻辑的校验(或者包含的业务逻辑较少)；service层是对业务逻辑的校验(可能被controller之外的类调用)
        // 虽然在service中进行了校验，此处校验的好处如上，相当于加一层保险
        if(StringUtils.isAnyBlank(userAccount, userPassword, checkPassword,planetCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
//        return new BaseResponse<>(0,result,"ok");
        return ResultUtils.success(result);
    }

    // 用户登录
    @PostMapping("/login")
    public BaseResponse<User> userRegister(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if(userLoginRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if(StringUtils.isAnyBlank(userAccount, userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
//        return new BaseResponse<>(0,user,"ok");
        return ResultUtils.success(user);
    }

    // 用户注销
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request){
        if(request == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    // 获取当前用的登录态
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);// 从request请求当中获取用户的登录态
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 获取到的是浏览器中用户登录传进来的数据，那么我们可以让通过id从数据库中获取真的数据
        long userId = currentUser.getId();
        // todo 校验用户是否合法
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    // 查询用户(必须考虑这个接口是否会被恶意调用，也就是鉴权只有某某能操作)
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username,HttpServletRequest request){
        // 鉴权 进管理员可查询
        if(!isAdmin(request)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(username)){
            queryWrapper.like("username",username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> collect = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(collect);
    }

    // 删除用户(必须考虑这个接口是否会被恶意调用，也就是鉴权只有某某能操作)
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUsers(@RequestBody long id,HttpServletRequest request){
        // 鉴权 进管理员可查询
        if(!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTHS);
        }

        if(id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 鉴权方法抽离
     * @param request 从请求中获取登录态
     * @return 是否为管理员
     */
    private boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);// 获取到浏览器中用户的信息
        User user = (User) userObj;// 转成用户对象
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

}