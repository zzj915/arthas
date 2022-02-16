package com.alibaba.arthas.tunnel.server.app.security;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 用户类，保存当前线程用户信息
 *
 * @author zzj
 * @version 2019-06-16
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@NoArgsConstructor
public class AuthUser {

    /**
     * userId
     */
    private String id;

    /**
     * username, 登录用户名
     */
    private String username;

    /**
     * 密码
     */
    private String secret;

    /**
     * 昵称（姓名）
     */
    private String name;

    /**
     * 电话号码
     */
    private String phone;

    // -------------------------------

    /**
     * jwtToken
     */
    private String jwtToken;
    /**
     * 签发者
     */
    private String issuer;

    // -------------------------------

    /**
     * 委托ID
     */
    private String delegate;

    // -------------------------------

    /**
     * 部门id
     */
    private List<String> departIds;

}
