package com.yupi.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名(昵称)
     */
    private String username;

    /**
     * 登录账号
     */
    private String userAccount;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 性别(0男/1女)
     */
    private Integer gender;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户状态(0正常/1封号/2星球过期)
     */
    private Integer userStatus;

    /**
     * 数据插入时间
     */
    private Date createTime;

    /**
     * 数据更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除(0正常/1删除) 第二个步骤告诉MP
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 权限（用户角色 0普通用户/1管理员）
     */
    private Integer userRole;

    /**
     * 星球编号
     */
    private String planetCode;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}