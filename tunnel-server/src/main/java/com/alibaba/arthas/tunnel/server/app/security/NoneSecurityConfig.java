package com.alibaba.arthas.tunnel.server.app.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.http.HttpServletResponse;

/**
 * NoneSecurityConfig，用于本地开发跳过登录，方便测试
 * 
 * @author zzj
 * @version 2019-07-23
 */
@Slf4j
@Configuration
@ConditionalOnProperty(value = "zoomlgd.security.type", havingValue = "none")
@EnableWebSecurity
public class NoneSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().disable()
                .csrf().disable()
                .httpBasic().disable()
                .formLogin().disable()
                .authorizeRequests().anyRequest().permitAll().and()
                .addFilterBefore(new CharacterEncodingFilter("UTF-8", true), CsrfFilter.class)
                .addFilterBefore(new NoneAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling()
                // 未认证
                .authenticationEntryPoint((request, response, e) -> {
                    log.info("401 Unauthorized, {}", e.getMessage());
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                })
                // 权限不足
                .accessDeniedHandler((request, response, e) -> {
                    log.info("403 Forbidden, {}", e.getMessage());
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                })
                .and();
    }

}
