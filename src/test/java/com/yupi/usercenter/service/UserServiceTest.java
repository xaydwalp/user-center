package com.yupi.usercenter.service;
import java.util.Date;


import com.yupi.usercenter.UserCenterApplication;
import com.yupi.usercenter.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务测试
 * @author zzp
 */
@SpringBootTest(classes = UserCenterApplication.class)
@RunWith(SpringRunner.class)
class UserServiceTest {

    @Resource
    UserService userService;

    // 测试添加一个用户
    @Test
    public void testAddUser(){
        User user = new User();
        user.setUsername("zzp");
        user.setUserAccount("123");
        user.setAvatarUrl("https://www.codefather.cn/logo.png");
        user.setUserPassword("xxx");
        user.setPhone("123");
        user.setEmail("123");
        boolean result = userService.save(user);
        System.out.println("save = " + result);
        System.out.println(user.getId());
        Assertions.assertEquals(true, result);
    }

    // 测试用户注册
    @Test
    void userRegister() {
        String userAccount = "yupi";
        String userPassword = "";
        String checkPassword = "123456";
        String planetCode = "1";
        // 判断密码非空
        long result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1, result);
        // 判断账户长度小于四位
        userAccount = "yu";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1, result);
        // 判断密码小于八位
        userAccount = "yupi";
        userPassword = "123456";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1, result);
        // 判断特殊字符
        userAccount = "yu pi";
        userPassword = "12345678";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1, result);
        // 校验密码
        checkPassword = "123456789";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1, result);
        // 账户不能重复
        userAccount = "dogYupi";
        checkPassword = "12345678";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1, result);
        // 正常
        userAccount = "yupi";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1, result);
    }
}