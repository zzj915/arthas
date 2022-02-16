package com.alibaba.arthas.tunnel.server.app.security;

import com.alibaba.arthas.tunnel.server.app.configuration.WhiteListProperties;
import com.alibaba.arthas.tunnel.server.app.configuration.ZoomlgdServiceProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.http.HttpServletResponse;

/**
 * 配合 Gateway 网关使用的 Security Configuration，生产环境中使用此种方式
 *
 * @author zzj
 * @version 2019-5-5
 */
@Slf4j
@Configuration
@ConditionalOnProperty(value = "zoomlgd.security.type", havingValue = "gateway")
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class GatewaySecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * 此配置匹配的PATTERNS
     */
    private static final String[] PATTERNS = {"/**"};

    /**
     * 白名单
     */
    @Autowired
    private WhiteListProperties whiteListProperties;

    @Autowired
    @Qualifier("defaultTokenVerifyServiceImpl")
    ITokenVerifyService defaultVerifyService;

    /**
     * ZoomlgdProperties 配置信息
     */
    @Autowired
    private ZoomlgdServiceProperties serviceProperties;

    @Value("${spring.application.name}")
    private String serviceName;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
                .authorizeRequests()
//                    .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ENDPOINT_ADMIN")
//                    .antMatchers(whiteListProperties.getWhiteList()).permitAll()
                .anyRequest().authenticated()
                .and()
                .cors().and()
                .csrf().disable()
                .httpBasic().disable()
                .formLogin().disable()
                .anonymous().disable()
                .addFilterBefore(new CharacterEncodingFilter("UTF-8", true), CsrfFilter.class)
                .addFilterBefore(new GatewayAuthenticationWebFilterV2(serviceProperties, whiteListProperties, defaultVerifyService, serviceName), UsernamePasswordAuthenticationFilter.class)
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
                .and()
//                .sessionManagement().maximumSessions(1).and().and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .logout()
                .logoutSuccessHandler((request, response, authentication) -> {
                    log.info("logout succeed.");
                    response.getWriter().flush();
                })
                .permitAll();

//        // allow iframe
//        if (arthasProperties.isEnableIframeSupport()) {
//            httpSecurity.headers().frameOptions().disable();
//        }

        // @formatter:on
    }

}
