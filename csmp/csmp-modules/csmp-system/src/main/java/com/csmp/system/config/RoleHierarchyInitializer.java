package com.csmp.system.config;

import com.csmp.system.service.IRoleEffectiveMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 服务启动时全量刷新角色有效菜单物化表
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class RoleHierarchyInitializer implements ApplicationRunner {

    private final IRoleEffectiveMenuService effectiveMenuService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("开始全量刷新角色有效菜单物化表...");
        effectiveMenuService.refreshAllEffectiveMenus();
        log.info("角色有效菜单物化表全量刷新完成");
    }
}
