package com.alibaba.arthas.tunnel.server.app.security;


import com.alibaba.arthas.tunnel.server.app.configuration.ZoomlgdServiceProperties;

/**
 * Token 验证 Service
 *
 * @author zzj
 * @version 2020/4/2
 */
public interface ITokenVerifyService {

    /**
     * token 类型KEY
     */
    String CLAIMS_TOKEN_TYPE_KEY = "type";
    String CLAIMS_TOKEN_TYPE_DEFAULT = "web";

    String CLAIMS_TOKEN_VERIFY_BEAN_KEY = "verifier";
    String CLAIMS_TOKEN_VERIFY_BEAN_DEFAULT = "defaultTokenVerifyServiceImpl";

    /**
     * 通过远程调用，验证jwttoken是否有效
     * @param cacheKey  缓存key
     * @param tokenConfig tokenConfig
     * @param jwtToken  token值
     * @return true 有效
     */
    boolean verifyTokenAndCacheIt(String cacheKey, ZoomlgdServiceProperties.Token tokenConfig, String jwtToken);


}
