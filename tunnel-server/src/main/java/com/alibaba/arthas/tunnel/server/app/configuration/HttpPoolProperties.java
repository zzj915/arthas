package com.alibaba.arthas.tunnel.server.app.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * http 连接池配置
 *
 * @author zzj
 * @version 2019/12/5
 */
@Data
@ConfigurationProperties(ignoreInvalidFields = true, prefix = "zoomlgd.http-pool")
@Component
public class HttpPoolProperties {
    /**
     * 最大连接数
     */
    private Integer maxTotal = 50;
    /**
     * 连接到同一ip的最大连接数
     */
    private Integer defaultMaxPerRoute = 20;
    /**
     * 连接超时时间，连接上服务器(握手成功)的时间，超出抛出connect timeout
     * -1表示不超时
     */
    private Integer connectTimeout = 5000;
    /**
     * 从连接池中获取连接的超时时间，超时间未拿到可用连接，会抛出org.apache.http.conn.ConnectionPoolTimeoutException: Timeout waiting for connection from pool
     * -1表示不超时
     */
    private Integer connectionRequestTimeout = 1000;
    /**
     * 会话超时时间，服务器返回数据(response)的时间，超过抛出read timeout
     * -1表示不超时
     */
    private Integer socketTimeout = 65000;
    /**
     * 验证时间
     * -1表示不验证
     */
    private Integer validateAfterInactivity = 2000;
}
