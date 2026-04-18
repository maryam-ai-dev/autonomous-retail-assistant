package com.aisleon.scraping;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/connectors")
public class ConnectorAdminController {

    private final ConnectorRegistry registry;

    public ConnectorAdminController(ConnectorRegistry registry) {
        this.registry = registry;
    }

    @GetMapping("/status")
    public List<ConnectorStatus> status() {
        return registry.allStatuses();
    }
}
