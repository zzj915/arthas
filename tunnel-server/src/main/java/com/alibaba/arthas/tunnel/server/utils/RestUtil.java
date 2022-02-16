package com.alibaba.arthas.tunnel.server.utils;

import com.alibaba.arthas.tunnel.server.app.security.AuthConsts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

/**
 * 服务间Rest调用工具类
 * <p>
 * 调用前，设置用户token等信息
 *
 * @author zzj
 * @version 2019/9/3
 */
@Slf4j
@Component
public class RestUtil {

    /**
     * 服务名
     */
    private static String SERVICE_NAME;
    /**
     * rest实例
     */
    private static RestTemplate REST_TEMPLATE;

    @Value("${spring.application.name}")
    private String serviceName;

    @Autowired
    @LoadBalanced
    private RestTemplate restTemplate;

    @PostConstruct
    public void postConstruct() {
        SERVICE_NAME = this.serviceName;
        REST_TEMPLATE = this.restTemplate;
    }

    /**
     * 微服务域内rest请求，携带用户jwttoken
     *
     * @param url           the URL
     * @param method        the HTTP method (GET, POST, etc)
     * @param requestEntity the entity (headers and/or body) to write to the, may be null
     * @param typeReference the type of the return value, without {@code R}
     * @param uriVariables  the variables to expand in the template
     * @return the T instance
     */
    public static <T> T exchange(String url,
                                 HttpMethod method,
                                 @Nullable HttpEntity<?> requestEntity,
                                 ParameterizedTypeReference<R<T>> typeReference,
                                 Object... uriVariables) throws RestClientException {

        log.info("【服务内RestTemplate调用】url: {}, variables: {}, method: {}, requestEntity: {}", url, uriVariables, method, requestEntity);

        HttpHeaders headers = new HttpHeaders();
        // 用户token写入头部
        headers.add(AuthConsts.X_CLIENT_SERVICE_ID, SERVICE_NAME);
        headers.add(AuthConsts.X_CLIENT_USER_TOKEN, UserUtil.currentUser().getJwtToken());

        if (requestEntity == null) {
            requestEntity = new HttpEntity<>(headers);
        } else {
            requestEntity.getHeaders();
            headers.putAll(requestEntity.getHeaders());
            requestEntity = new HttpEntity<>(requestEntity.getBody(), headers);
        }

        return doExchange(url, method, requestEntity, typeReference, uriVariables);
    }

    /**
     * 微服务域内rest请求，不携带用户jwttoken
     *
     * @param url           the URL
     * @param method        the HTTP method (GET, POST, etc)
     * @param requestEntity the entity (headers and/or body) to write to the, may be null
     * @param typeReference the type of the return value, without {@code R}
     * @param uriVariables  the variables to expand in the template
     * @return the T instance
     */
    public static <T> T exchangeWithoutToken(String url,
                                             HttpMethod method,
                                             @Nullable HttpEntity<?> requestEntity,
                                             ParameterizedTypeReference<R<T>> typeReference,
                                             Object... uriVariables) throws RestClientException {

        log.info("【服务内RestTemplate调用】url: {}, variables: {}, method: {}, requestEntity: {}", url, uriVariables, method, requestEntity);

        HttpHeaders headers = new HttpHeaders();
        headers.add(AuthConsts.X_CLIENT_SERVICE_ID, SERVICE_NAME);

        if (requestEntity == null) {
            requestEntity = new HttpEntity<>(headers);
        } else {
            requestEntity.getHeaders();
            headers.putAll(requestEntity.getHeaders());
            requestEntity = new HttpEntity<>(requestEntity.getBody(), headers);
        }

        return doExchange(url, method, requestEntity, typeReference, uriVariables);
    }

    private static <T> T doExchange(String url,
                                    HttpMethod method,
                                    HttpEntity<?> requestEntity,
                                    ParameterizedTypeReference<R<T>> typeReference,
                                    Object... uriVariables) {

        long beginTime = System.currentTimeMillis();

        ResponseEntity<R<T>> responseEntity = REST_TEMPLATE.exchange(url, method, requestEntity, typeReference, uriVariables);
        R<T> r = responseEntity.getBody();

        log.debug("【服务内RestTemplate调用】url: {}, 执行时间：{} ms", url, System.currentTimeMillis() - beginTime);

        // 远程调用失败
        if (r == null) {
            log.warn("【服务内RestTemplate调用】远程调用返回 null。url: {}", url);
            return null;
        }

        // 请求失败
        if (r.getCode() != 0) {
            log.warn("【服务内RestTemplate调用】请求失败，{}。url: {}", r.getMsg(), url);
            return null;
        }

        return r.getData();
    }

}
