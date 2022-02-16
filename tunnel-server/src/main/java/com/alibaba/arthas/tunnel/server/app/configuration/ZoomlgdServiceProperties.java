package com.alibaba.arthas.tunnel.server.app.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Zoomlgd Service Properties
 *
 * @author zzj
 * @version 2019/12/4
 */
@Data
@Component
@EnableConfigurationProperties
@ConfigurationProperties(ignoreInvalidFields = true, prefix = "zoomlgd.service")
public class ZoomlgdServiceProperties {

    private List<Token> tokens = new ArrayList<>();

    /**
     * <p>获取token map。key为token的id字段</p>
     * <p>注意：此段程序可以优化，不需要每次通过循环构造对象再返回，当前考虑到tokens的长度非常小，就没进行处理</p>
     *
     * @return HashMap<String, Token>
     */
    public HashMap<String, Token> getTokenMap() {
        HashMap<String, Token> map = new HashMap<>();

        Token token = null;
        for (Token t : this.getTokens()) {
            map.put(t.getId(), t);
        }

        return map;
    }

    /**
     * 服务间调用jwtToken签名
     */
    @Data
    public static class Token {

        /**
         * id字段，亦是jwttoken的issuer
         */
        private String id;
        /**
         * jwt-token 签名密钥
         */
        private String secret = "";

        /**
         * 过期时间，单位秒
         */
        private Long expiration = 1800L;

        /**
         * 刷新 token 过期时间，单位秒
         */
        private Long refreshTokenTimeout = 7200L;

        /**
         * token校验服务地址，secret 与 verifyUrl 只配置一个即可
         */
        private String verifyUrl = "";

        /**
         * 权限验证服务地址，默认使用系统统一权限校验
         * userId: 用户ID
         * code: 权限代码
         */
        private String authorizeUrl = "http://system-users/system/authority/check?userId={userId}&code={code}";

    }

}
