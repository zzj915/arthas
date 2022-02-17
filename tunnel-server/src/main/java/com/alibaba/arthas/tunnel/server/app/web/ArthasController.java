package com.alibaba.arthas.tunnel.server.app.web;

import com.alibaba.arthas.tunnel.server.AgentInfo;
import com.alibaba.arthas.tunnel.server.TunnelServer;
import com.alibaba.arthas.tunnel.server.app.configuration.ArthasProperties;
import com.alibaba.arthas.tunnel.server.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Arthas 基础信息查询
 *
 * @author zzj
 * @version 2022/2/16
 */
@RestController
@RequestMapping("/arthas")
public class ArthasController {

    @Autowired
    ArthasProperties arthasProperties;

    @Autowired
    TunnelServer tunnelServer;

    /**
     * 获取列表
     *
     * @return 列表
     */
    @GetMapping("/info")
    public R<Map<String, Object>> info() {
        Map<String, Object> result = new HashMap<>(4);

        result.put("version", this.getClass().getPackage().getImplementationVersion());
        result.put("properties", arthasProperties);

        result.put("agents", tunnelServer.getAgentInfoMap());
        result.put("clientConnections", tunnelServer.getClientConnectionInfoMap());

        return R.ok(result);
    }

    /**
     * 获取列表
     *
     * @return 列表
     */
    @GetMapping("/agents")
    public R<Map<String, AgentInfo>> agents() {
        return R.ok(tunnelServer.getAgentInfoMap());
    }

}
