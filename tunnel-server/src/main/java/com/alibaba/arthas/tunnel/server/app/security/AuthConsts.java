package com.alibaba.arthas.tunnel.server.app.security;

/**
 * 服务间权限配置常量
 *
 * @author zzj
 * @version 2019/9/3
 */
public class AuthConsts {

    /**
     * 当前用户
     */
    public final static String X_CLIENT_USER_TOKEN = "X-Client-User-Token";

    /**
     * 服务名称
     */
    public final static String X_CLIENT_SERVICE_ID = "X-Client-Service-Id";

    /**
     * 业务运营平台 使用此参数，传递操作的app
     */
    public static final String X_CLIENT_APP_DELEGATE = "X-App-Delegate";

    /**
     * 中心用户操作记录使用 传递操作的uerId
     */
    public static final String X_APP_USER_ID = "X-App-User-Id";

    /**
     * 中心/应用 调用来源名称
     */
    public final static String X_APP_NAME = "X-App-Name";

    /**
     * Authorization
     */
    public static final String AUTHORIZATION = "Authorization";

    /**
     * Authorization 前缀
     */
    public static final String BEARER = "Bearer ";

    /**
     * 匿名用户
     */
    public static final String ANONYMOUS_USER_ID = "anonymousUser";

    /**
     * 内网客户端
     */
    public static final String ANONYMOUS_CLIENT_ID = "anonymousClient";

    /**
     * 中心匿名用户
     */
    public static final String ANONYMOUS_BMP_USER_ID = "anonymousBmpUser";

}
