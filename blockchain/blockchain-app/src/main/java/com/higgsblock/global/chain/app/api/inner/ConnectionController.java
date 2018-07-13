package com.higgsblock.global.chain.app.api.inner;

import com.higgsblock.global.chain.app.api.vo.ConnectionInfo;
import com.higgsblock.global.chain.app.connection.ConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

/**
 * for testing
 *
 * @author baizhengwen
 * @date 2018/3/16
 */
@Deprecated
@RestController
@RequestMapping("/connections")
public class ConnectionController {

    @Autowired
    private ConnectionManager connectionManager;

    @RequestMapping("/list")
    public Object list() {
        return connectionManager.getActivatedConnections().stream().map(connection -> {
            ConnectionInfo info = new ConnectionInfo();
            info.setId(connection.getId());
            info.setPeerId(connection.getPeerId());
            info.setIp(connection.getIp());
            info.setPort(connection.getPort());
            info.setActivated(connection.isActivated());
            info.setClient(connection.isClient());
            info.setConnectionLevel(connection.getConnectionLevel());
            return info;
        }).collect(Collectors.toList());
    }
}
