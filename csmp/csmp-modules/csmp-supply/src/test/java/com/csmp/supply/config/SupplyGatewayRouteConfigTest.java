package com.csmp.supply.config;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("dev")
class SupplyGatewayRouteConfigTest {

    private static final String SUPPLY_ROUTE_ID = "csmp-supply";
    private static final String SUPPLY_API_COMPAT_ROUTE_ID = "csmp-supply-api-compat";

    @Test
    void repositoryGatewayConfigShouldKeepSupplyResourceSegment() throws IOException {
        Path configPath = findRepoGatewayConfig();
        Map<String, Integer> stripPrefixMap = readStripPrefixMap(configPath);

        assertEquals(1, stripPrefixMap.get(SUPPLY_ROUTE_ID), "标准 /supply/** 路由必须仅裁剪一层前缀");
        assertEquals(2, stripPrefixMap.get(SUPPLY_API_COMPAT_ROUTE_ID), "兼容 /api/supply/** 路由必须裁剪两层前缀");
    }

    @Test
    void localNacosGatewaySnapshotShouldMatchRepositoryRouteDefinition() throws IOException {
        Path snapshotPath = Path.of(System.getProperty("user.home"), "nacos", "config",
            "Config-fixed-dev-127.0.0.1_8848_nacos", "snapshot-tenant", "dev", "DEFAULT_GROUP", "csmp-gateway.yml");
        assertTrue(Files.exists(snapshotPath), "本机 Nacos 快照不存在，无法校验运行配置");

        Map<String, Integer> stripPrefixMap = readStripPrefixMap(snapshotPath);

        assertEquals(1, stripPrefixMap.get(SUPPLY_ROUTE_ID),
            "本机 Nacos 快照中的 /supply/** 路由被错误裁剪，会把 /supply/suppliers/list 转成 /list");
        assertEquals(2, stripPrefixMap.get(SUPPLY_API_COMPAT_ROUTE_ID),
            "本机 Nacos 快照中的 /api/supply/** 兼容路由必须保留两层裁剪");
    }

    private Path findRepoGatewayConfig() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            Path candidate = current.resolve("script").resolve("config").resolve("nacos").resolve("csmp-gateway.yml");
            if (Files.exists(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("未找到仓库内的 script/config/nacos/csmp-gateway.yml");
    }

    private Map<String, Integer> readStripPrefixMap(Path configPath) throws IOException {
        List<String> lines = Files.readAllLines(configPath);
        Map<String, Integer> stripPrefixMap = new HashMap<>();
        String currentRouteId = null;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("- id: ")) {
                currentRouteId = trimmed.substring("- id: ".length()).trim();
                continue;
            }
            if (currentRouteId == null) {
                continue;
            }
            if (trimmed.startsWith("- StripPrefix=")) {
                int value = Integer.parseInt(trimmed.substring("- StripPrefix=".length()).trim());
                stripPrefixMap.put(currentRouteId, value);
            }
        }
        assertTrue(stripPrefixMap.containsKey(SUPPLY_ROUTE_ID), () -> "配置中缺少路由 " + SUPPLY_ROUTE_ID);
        assertTrue(stripPrefixMap.containsKey(SUPPLY_API_COMPAT_ROUTE_ID), () -> "配置中缺少路由 " + SUPPLY_API_COMPAT_ROUTE_ID);
        return stripPrefixMap;
    }
}
