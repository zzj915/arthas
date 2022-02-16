package com.alibaba.arthas.tunnel.server.app.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@Data
@Component
@ConfigurationProperties(ignoreInvalidFields = true, prefix = "zoomlgd.security.white-list")
public class WhiteListProperties {

    private WhiteListConfig config = new WhiteListConfig();

    @NotNull
    private String[] apis = new String[]{};

    @NotNull
    private String[] clients = new String[]{};

    /**
     * 配置中心，白名单配置dataId
     */
    @Data
    public static class WhiteListConfig {

        /**
         * nacos 配置 Data Id
         */
        private String dataId;

        /**
         * nacos 配置 Group
         */
        private String group = "DEFAULT_GROUP";
    }

}
