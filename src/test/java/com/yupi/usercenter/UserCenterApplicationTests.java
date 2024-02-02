package com.yupi.usercenter;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@SpringBootTest(classes = UserCenterApplication.class)
@RunWith(SpringRunner.class)
public class UserCenterApplicationTests {

    @Test
    public void test() throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] digest = md5.digest("123".getBytes());
        System.out.println(Arrays.toString(digest));
        System.out.println(new String(digest));
    }

    @Test
    public void contextLoads(int[] nums,int target) {

    }
}