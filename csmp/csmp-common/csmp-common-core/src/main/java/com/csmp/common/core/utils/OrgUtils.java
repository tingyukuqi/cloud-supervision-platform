package com.csmp.common.core.utils;

import cn.hutool.core.util.StrUtil;

/**
 * 组织机构工具类
 * <p>
 * 从部门的 ancestors 字段推导组织ID。
 * 组织机构 = sys_dept 中 parent_id = 0 的根部门。
 * ancestors 格式: "0" (组织自身), "0,100" (组织的直属子部门), "0,100,101" (更深层子部门)
 *
 * @author csmp
 */
public class OrgUtils {

    private OrgUtils() {
    }

    /**
     * 从部门 ancestors 推导出组织ID
     * <p>
     * 规则:
     * - ancestors = "0" -> 此部门本身就是组织, orgId = deptId
     * - ancestors = "0,100" -> deptId=100 是组织, orgId = 100
     * - ancestors = "0,100,101" -> 组织ID = 100 (ancestors 中 "0" 之后的第一个ID)
     * - ancestors 为空或格式异常 -> 返回 null
     *
     * @param ancestors 部门祖级列表（逗号分隔）
     * @param deptId    部门ID
     * @return 组织ID，无法推导时返回 null
     */
    public static Long getOrgId(String ancestors, Long deptId) {
        if (StrUtil.isBlank(ancestors)) {
            return null;
        }
        String[] parts = ancestors.split(",");
        if (parts.length == 0) {
            return null;
        }
        // ancestors = "0" -> 此部门就是组织（根部门），orgId = deptId
        if (parts.length == 1 && "0".equals(parts[0])) {
            return deptId;
        }
        // ancestors = "0,xxx,..." -> "0" 之后第一个元素就是组织ID
        if ("0".equals(parts[0]) && parts.length >= 2) {
            try {
                return Long.parseLong(parts[1]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 判断部门是否为组织（根部门）
     *
     * @param parentId 父部门ID
     * @return parent_id = 0 时为组织
     */
    public static boolean isOrg(Long parentId) {
        return parentId != null && parentId == 0L;
    }
}
