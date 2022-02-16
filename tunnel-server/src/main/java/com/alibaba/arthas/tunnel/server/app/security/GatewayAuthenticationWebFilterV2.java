package com.alibaba.arthas.tunnel.server.app.security;

import com.alibaba.arthas.tunnel.server.app.configuration.WhiteListProperties;
import com.alibaba.arthas.tunnel.server.app.configuration.ZoomlgdServiceProperties;
import com.alibaba.arthas.tunnel.server.utils.IpUtil;
import com.alibaba.arthas.tunnel.server.utils.JwtUtil;
import com.alibaba.arthas.tunnel.server.utils.R;
import com.alibaba.arthas.tunnel.server.utils.SpringContextHolder;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GatewayAuthenticationWebFilter，解决 Http Header 中包含的认证用户身份信息
 *
 * @author zzj
 * @version 2019-07-23
 */
@Slf4j
public class GatewayAuthenticationWebFilterV2 extends OncePerRequestFilter {


    private static final ConcurrentHashMap<String, ITokenVerifyService> VERIFY_SERVICE_MAP = new ConcurrentHashMap<>();

    /**
     * 白名单
     */
    private WhiteListProperties whiteListProperties;

    /**
     * zoomlgd properties，取 secret
     */
    private ZoomlgdServiceProperties serviceProperties;

    /**
     * 默认token校验service
     */
    private ITokenVerifyService defaultVerifyService;

    private String serviceName;

    private final PathMatcher pathMatcher = new AntPathMatcher();

    GatewayAuthenticationWebFilterV2(ZoomlgdServiceProperties serviceProperties,
                                     WhiteListProperties whiteListProperties,
                                     ITokenVerifyService defaultVerifyService,
                                     String serviceName) {
        this.serviceProperties = serviceProperties;
        this.whiteListProperties = whiteListProperties;
        this.defaultVerifyService = defaultVerifyService;
        this.serviceName = serviceName;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 服务ID
        String serviceName = request.getHeader(AuthConsts.X_CLIENT_SERVICE_ID);
        // 域内服务间调用传递的token
        String clientUserToken = request.getHeader(AuthConsts.X_CLIENT_USER_TOKEN);
        // Authorization
        String authHeader = request.getHeader(AuthConsts.AUTHORIZATION);
        // 客户端ip
        String clientIp = this.getClientIp(request);
        // requestUri
        String requestUri = request.getRequestURI();

        boolean validated = false;
        String errorMsg = "";

        // 1. X_CLIENT_USER_TOKEN
        if (StringUtils.hasLength(clientUserToken)) {
            Claims claims = validateJwtToken(clientUserToken);
            if (claims == null) {
                log.warn("【用户身份认证】Client User Token 检验失败。service: {}, jwtToken: {}", serviceName, clientUserToken);
                errorMsg = "Client User Token 检验失败";
            } else {
                storeAuthentication(request, clientUserToken, claims);
                validated = true;
                errorMsg = "";
            }
        }
        // 2. AUTHORIZATION TOKEN
        if (!validated && StringUtils.hasLength(authHeader)) {
            log.debug("【用户身份认证】AuthConsts.AUTHORIZATION: {}", authHeader);
            if (authHeader.startsWith(AuthConsts.BEARER)) {
                authHeader = StringUtils.delete(authHeader, AuthConsts.BEARER);
            }

            Claims claims = validateJwtToken(authHeader);
            if (claims == null) {
                log.warn("【用户身份认证】jwtToken 检验失败。service: {}, jwtToken: {}", serviceName, authHeader);
                errorMsg = "jwtToken 检验失败";
            } else {
                storeAuthentication(request, authHeader, claims);
                validated = true;
                errorMsg = "";
            }
        }
        // 3. WHITE CLIENT IP
        boolean isWhiteIp = isWhiteIp(clientIp);
        if (!validated && isWhiteIp) {
            // 客户端用户
            AuthUser user = new AuthUser().setId(AuthConsts.ANONYMOUS_CLIENT_ID);
            storeAuthUser(request, user);
            validated = true;
            errorMsg = "";
        }
        // 4. WHITE API
        boolean isWhiteApi = Arrays.stream(whiteListProperties.getApis()).anyMatch(x -> pathMatcher.match(x, requestUri));
        if (!validated && isWhiteApi) {
            AuthUser user = new AuthUser().setId(AuthConsts.ANONYMOUS_USER_ID);
            storeAuthUser(request, user);
            validated = true;
        }

        if (validated) {
            filterChain.doFilter(request, response);
        } else {
            if (!StringUtils.hasLength(errorMsg)) {
                errorMsg = "请求地址不在白名单中，请提供 token 认证";
            }
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(R.failed(errorMsg).toString());
        }
    }

    /**
     * 已获取到 jwtToken，解析 Authentication 并存储至 SecurityContextHolder.getContext() 中
     */
    private void storeAuthentication(HttpServletRequest request, String jwtToken, Claims claims) {
        String userId = claims.getSubject();
        String userName = (String) claims.get("username");
        log.info("【用户身份认证】认证成功，userId：{}, username: {}", userId, userName);

        AuthUser user = new AuthUser()
                .setId(userId)
                .setUsername(userName)
                .setName((String) claims.get("name"))
                .setPhone((String) claims.get("phone"))
                .setJwtToken(jwtToken)
                .setIssuer(claims.getIssuer());

        // 设置 BOP 委托id
        String delegateId = request.getHeader(AuthConsts.X_CLIENT_APP_DELEGATE);
        if (StringUtils.hasLength(delegateId)) {
            user.setDelegate(delegateId);
        }

        storeAuthUser(request, user);
    }

    /**
     * store anonymous
     */
    private void storeAuthUser(HttpServletRequest request, AuthUser user) {
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(user, "", Collections.emptySet());
        AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authRequest);
    }

    private Claims validateJwtToken(String jwtToken) {

        Claims claims = JwtUtil.getClaimsFromToken(jwtToken);
        if (claims == null) {
            return null;
        }

        String issuer = claims.getIssuer();
        if (!StringUtils.hasLength(issuer)) {
            log.warn("【用户身份认证】jwttoken无issuer，请开发人员注意！");
            return null;
        }

        // 判断是否过期
        if (JwtUtil.isJwtTokenExpired(jwtToken)) {
            log.warn("【用户身份认证】jwtToken配置已过期！");
            return null;
        }


        ZoomlgdServiceProperties.Token tokenConfig = serviceProperties.getTokenMap().get(issuer);
        if (tokenConfig == null) {
            log.warn("【用户身份认证】缺少token配置，请开发人员在yml配置文件中添加，issuer: {}", issuer);
            return null;
        }

        // TOKEN校验缓存KEY
        String cacheKey = claims.getIssuer() + "-" + claims.getSubject() + "-" + jwtToken;

        // 签发者非当前应用
        if (!serviceName.equals(issuer)) {
            if (defaultVerifyService.verifyTokenAndCacheIt(cacheKey, tokenConfig, jwtToken)) {
                return claims;
            }
            return null;
        }

        // 签发者为当前应用
        String tokenType = (String) claims.getOrDefault(ITokenVerifyService.CLAIMS_TOKEN_TYPE_KEY, ITokenVerifyService.CLAIMS_TOKEN_TYPE_DEFAULT);

        // 本地yml签名
        if (ITokenVerifyService.CLAIMS_TOKEN_TYPE_DEFAULT.equals(tokenType)) {
            return JwtUtil.getClaimsFromToken(jwtToken, tokenConfig.getSecret());
        }

        // 其它签名
        String verifyBeanName = (String) claims.getOrDefault(ITokenVerifyService.CLAIMS_TOKEN_VERIFY_BEAN_KEY, ITokenVerifyService.CLAIMS_TOKEN_VERIFY_BEAN_DEFAULT);
        ITokenVerifyService verifyService;
        if (VERIFY_SERVICE_MAP.containsKey(verifyBeanName)) {
            verifyService = VERIFY_SERVICE_MAP.get(verifyBeanName);
        } else {
            try {
                verifyService = SpringContextHolder.getBean(verifyBeanName);
                VERIFY_SERVICE_MAP.put(verifyBeanName, verifyService);
            } catch (Exception e) {
                return null;
            }
        }
        if (verifyService.verifyTokenAndCacheIt(cacheKey, tokenConfig, jwtToken)) {
            return claims;
        }

        return null;
    }

    /**
     * 获取请求客户端ip。
     * <p>x-forwarded-for，请求经过代理时，会添加代理服务器IP。此处未处理此种情况，主要是考虑IP白名单只允许同一VPC内使用</p>
     *
     * @param request request
     * @return ip
     */
    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("x-forwarded-for");
        if (clientIp != null) {
            return clientIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 判断ip是否在白名单中
     *
     * @param ip ip
     * @return boolean
     */
    private boolean isWhiteIp(String ip) {
        String delimiter = ",";
        if (ip.contains(delimiter)) {
            if (log.isDebugEnabled()) {
                log.debug("【用户身份认证】只允许同一VPC内ip白名单");
            }
            return false;
        }

        return Arrays.stream(whiteListProperties.getClients()).anyMatch(client -> {
            if (IpUtil.isIP(client)) {
                return client.equalsIgnoreCase(ip);
            } else if (client.contains("/")) {
                return IpUtil.isInRange(ip, client);
            }
            return false;
        });
    }

}
