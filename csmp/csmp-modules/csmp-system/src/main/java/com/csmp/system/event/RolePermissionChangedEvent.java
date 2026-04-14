package com.csmp.system.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 角色权限变更事件
 * <p>
 * 角色的菜单分配、父角色变更、隐藏菜单变更时发布此事件，
 * 用于触发物化表刷新和 Session 清理
 * </p>
 */
@Getter
public class RolePermissionChangedEvent extends ApplicationEvent {

    /**
     * 受影响的角色ID列表（包含自身和所有子孙角色）
     */
    private final List<Long> affectedRoleIds;

    /**
     * 是否级联刷新（父角色变更时需要级联刷新子角色）
     */
    private final boolean cascade;

    public RolePermissionChangedEvent(Object source, List<Long> affectedRoleIds, boolean cascade) {
        super(source);
        this.affectedRoleIds = affectedRoleIds;
        this.cascade = cascade;
    }

    public RolePermissionChangedEvent(Object source, Long roleId) {
        this(source, List.of(roleId), false);
    }
}
