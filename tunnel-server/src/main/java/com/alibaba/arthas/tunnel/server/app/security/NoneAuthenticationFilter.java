package com.alibaba.arthas.tunnel.server.app.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * 本地开发用，免登录过滤器类
 *
 * @author zhangxing
 * @version 2019-01-13
 */
@Slf4j
public class NoneAuthenticationFilter extends OncePerRequestFilter {

    private static final String ANONYMOUS_USER_ID = "anonymousUser";
    private static final String ANONYMOUS_PHONE = "13800001111";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        storeAuthentication(request);
        filterChain.doFilter(request, response);

    }

    /**
     * 已获取到 jwtToken，解析 Authentication 并存储至 SecurityContextHolder.getContext() 中
     */
    private void storeAuthentication(HttpServletRequest request) {
        log.info("【本地开发免登录认证】userId={}", ANONYMOUS_USER_ID);

        AuthUser user = new AuthUser();
        user.setId(ANONYMOUS_USER_ID);
        user.setPhone(ANONYMOUS_PHONE);

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(user, "", Collections.emptySet());

        AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authRequest);
    }

}
