package com.csmp.system.listener;

import com.csmp.system.event.RolePermissionChangedEvent;
import com.csmp.system.service.IRoleEffectiveMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 角色权限变更事件监听器
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class RoleEffectiveMenuRefreshListener {

    private final IRoleEffectiveMenuService effectiveMenuService;

    @EventListener
    public void onRolePermissionChanged(RolePermissionChangedEvent event) {
        log.info("收到角色权限变更事件, affectedRoleIds={}, cascade={}",
            event.getAffectedRoleIds(), event.isCascade());

        if (event.isCascade()) {
            for (Long roleId : event.getAffectedRoleIds()) {
                effectiveMenuService.refreshEffectiveMenuCascade(roleId);
            }
        } else {
            for (Long roleId : event.getAffectedRoleIds()) {
                effectiveMenuService.refreshEffectiveMenu(roleId);
            }
        }

        log.info("角色权限变更事件处理完成");
    }
}
