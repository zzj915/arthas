package com.alibaba.arthas.tunnel.server.app.security;

import com.alibaba.arthas.tunnel.server.app.configuration.ZoomlgdServiceProperties;
import com.alibaba.arthas.tunnel.server.utils.R;
import com.alibaba.arthas.tunnel.server.utils.RestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

/**
 * Token 验证 Service，有缓存
 * 
 * @author zzj
 * @version 2020/4/2
 */
@Service("defaultTokenVerifyServiceImpl")
@Slf4j
public class TokenVerifyServiceImpl implements ITokenVerifyService {

    @Override
    public boolean verifyTokenAndCacheIt (String cacheKey, ZoomlgdServiceProperties.Token tokenConfig, String jwtToken) {
        try {
            Boolean b = RestUtil.exchange(tokenConfig.getVerifyUrl(), HttpMethod.POST, null, new ParameterizedTypeReference<R<Boolean>>(){}, jwtToken);
            return b != null && b;

        } catch (Exception e) {
            log.warn("", e);
        }

        return false;
    }

}
