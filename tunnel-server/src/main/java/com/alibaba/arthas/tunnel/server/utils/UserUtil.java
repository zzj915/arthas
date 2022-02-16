package com.alibaba.arthas.tunnel.server.utils;

import com.alibaba.arthas.tunnel.server.app.security.AuthUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 系统用户工具类
 *
 * @author zzj
 * @version 2019/1/6
 */
public class UserUtil {

    private static final String ANONYMOUS_USER_ID = "anonymousUserInMultiThread";

    /**
     * 禁止实例化
     */
    private UserUtil() {}

    /**
     * 当前登录用户，数据保存在ThreadLocal变量中。多线程环境下，无法获取到值
     * @return User
     */
    public static AuthUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return new AuthUser().setId(ANONYMOUS_USER_ID);
        }
        return (AuthUser) authentication.getPrincipal();
    }

    /**
     * 是否匿名用户
     * @return boolean
     */
    public static boolean isAnonymous() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null;
    }

}
