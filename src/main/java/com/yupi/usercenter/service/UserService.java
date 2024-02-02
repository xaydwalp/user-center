package com.yupi.usercenter.service;

import com.yupi.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 86133
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2024-01-10 12:05:37
 * 用户服务
 */
public interface UserService extends IService<User> {



    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param planetCode 星球编号
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword,String planetCode);

    /**
     * 用户登录
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param request 浏览器请求(此处给请求的session设置值，从请求的session中获取值)
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 抽离出来的用户脱敏的方法
     * @param originUser 要处理的user
     * @return 要返回给前端什么我们就指定什么，防止数据库的字段泄露给前端
     */
    User getSafetyUser(User originUser);


    /**
     * 请求用户注销
     * @param request 请求对象
     */
    int userLogout(HttpServletRequest request);
}
