package com.csmp.system.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csmp.common.core.constant.SystemConstants;
import com.csmp.common.core.utils.StringUtils;
import com.csmp.common.satoken.utils.LoginHelper;
import com.csmp.system.api.model.LoginUser;
import com.csmp.system.domain.SysRole;
import com.csmp.system.domain.SysRoleEffectiveMenu;
import com.csmp.system.domain.SysRoleHiddenMenu;
import com.csmp.system.domain.SysRoleMenu;
import com.csmp.system.mapper.SysRoleEffectiveMenuMapper;
import com.csmp.system.mapper.SysRoleHiddenMenuMapper;
import com.csmp.system.mapper.SysRoleMapper;
import com.csmp.system.mapper.SysRoleMenuMapper;
import com.csmp.system.mapper.SysUserRoleMapper;
import com.csmp.system.service.IRoleEffectiveMenuService;
import com.csmp.system.service.IRoleHierarchyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 角色有效菜单管理服务实现
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class RoleEffectiveMenuServiceImpl implements IRoleEffectiveMenuService {

    private final SysRoleEffectiveMenuMapper effectiveMenuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysRoleHiddenMenuMapper hiddenMenuMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final IRoleHierarchyService hierarchyService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshEffectiveMenu(Long roleId) {
        // 1. 获取祖先链（从顶级到当前）
        List<Long> ancestorChain = hierarchyService.getAncestorChain(roleId);

        // 2. 按链顺序合并菜单
        LinkedHashMap<Long, SysRoleEffectiveMenu> effectiveRecordMap = new LinkedHashMap<>();

        for (Long chainRoleId : ancestorChain) {
            SysRole chainRole = roleMapper.selectById(chainRoleId);
            if (chainRole == null || !SystemConstants.NORMAL.equals(chainRole.getStatus())) {
                continue;
            }
            // 获取该角色自有菜单
            List<Long> ownMenuIds = roleMenuMapper.selectList(
                    new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, chainRoleId))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .toList();

            // 获取该角色隐藏的继承菜单
            List<Long> hiddenIds = hiddenMenuMapper.selectHiddenMenuIdsByRoleId(chainRoleId);
            Set<Long> hiddenMenuIds = new HashSet<>(hiddenIds);

            // 合并自有菜单
            for (Long menuId : ownMenuIds) {
                if (!effectiveRecordMap.containsKey(menuId) || chainRoleId.equals(roleId)) {
                    SysRoleEffectiveMenu record = new SysRoleEffectiveMenu();
                    record.setRoleId(roleId);
                    record.setMenuId(menuId);
                    record.setSource(chainRoleId.equals(roleId) ? "OWN" : "INHERITED");
                    record.setInheritFromRoleId(chainRoleId.equals(roleId) ? null : chainRoleId);
                    effectiveRecordMap.put(menuId, record);
                }
            }

            // 应用隐藏：移除被隐藏的菜单
            if (!hiddenMenuIds.isEmpty()) {
                hiddenMenuIds.forEach(menuId -> {
                    SysRoleEffectiveMenu record = effectiveRecordMap.get(menuId);
                    if (record != null && !"OWN".equals(record.getSource())) {
                        effectiveRecordMap.remove(menuId);
                    }
                });
            }
        }

        // 3. 删除旧的有效菜单
        effectiveMenuMapper.delete(new LambdaQueryWrapper<SysRoleEffectiveMenu>()
            .eq(SysRoleEffectiveMenu::getRoleId, roleId));

        // 4. 批量插入新的有效菜单
        List<SysRoleEffectiveMenu> effectiveRecords = new ArrayList<>(effectiveRecordMap.values());
        if (CollUtil.isNotEmpty(effectiveRecords)) {
            effectiveMenuMapper.insertBatch(effectiveRecords);
        }

        log.info("角色有效菜单刷新完成, roleId={}, 有效菜单数={}", roleId, effectiveRecords.size());
    }

    @Override
    public void refreshEffectiveMenuCascade(Long roleId) {
        List<Long> descendantIds = hierarchyService.getDescendantRoleIds(roleId);
        for (Long descendantId : descendantIds) {
            try {
                refreshEffectiveMenu(descendantId);
            } catch (Exception e) {
                log.error("刷新角色有效菜单失败, roleId={}", descendantId, e);
            }
        }
        // 刷新完成后清除受影响角色关联的在线用户
        for (Long descendantId : descendantIds) {
            cleanOnlineUserByRole(descendantId);
        }
    }

    @Override
    public void refreshAllEffectiveMenus() {
        List<SysRole> allRoles = roleMapper.selectList(
            new LambdaQueryWrapper<SysRole>()
                .select(SysRole::getRoleId));
        for (SysRole role : allRoles) {
            try {
                refreshEffectiveMenu(role.getRoleId());
            } catch (Exception e) {
                log.error("全量刷新角色有效菜单失败, roleId={}", role.getRoleId(), e);
            }
        }
        log.info("全量角色有效菜单刷新完成, 共处理{}个角色", allRoles.size());
    }

    @Override
    public Set<Long> getEffectiveMenuIds(Long roleId) {
        List<Long> ids = effectiveMenuMapper.selectEffectiveMenuIdsByRoleId(roleId);
        return new HashSet<>(ids);
    }

    private void cleanOnlineUserByRole(Long roleId) {
        Long num = userRoleMapper.selectCount(new LambdaQueryWrapper<com.csmp.system.domain.SysUserRole>()
            .eq(com.csmp.system.domain.SysUserRole::getRoleId, roleId));
        if (num == 0) {
            return;
        }
        List<String> keys = StpUtil.searchTokenValue("", 0, -1, false);
        if (CollUtil.isEmpty(keys)) {
            return;
        }
        keys.parallelStream().forEach(key -> {
            String token = StringUtils.substringAfterLast(key, ":");
            if (StpUtil.stpLogic.getTokenActiveTimeoutByToken(token) < -1) {
                return;
            }
            LoginUser loginUser = LoginHelper.getLoginUser(token);
            if (ObjectUtil.isNull(loginUser) || CollUtil.isEmpty(loginUser.getRoles())) {
                return;
            }
            if (loginUser.getRoles().stream().anyMatch(r -> r.getRoleId().equals(roleId))) {
                try {
                    StpUtil.logoutByTokenValue(token);
                } catch (NotLoginException ignored) {
                }
            }
        });
    }
}
