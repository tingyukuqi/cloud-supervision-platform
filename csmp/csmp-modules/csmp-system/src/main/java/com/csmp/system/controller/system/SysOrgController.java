package com.csmp.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.lang.tree.Tree;
import lombok.RequiredArgsConstructor;
import com.csmp.common.core.domain.R;
import com.csmp.common.core.validate.AddGroup;
import com.csmp.common.core.validate.EditGroup;
import com.csmp.common.idempotent.annotation.RepeatSubmit;
import com.csmp.common.log.annotation.Log;
import com.csmp.common.log.enums.BusinessType;
import com.csmp.common.web.core.BaseController;
import com.csmp.system.domain.bo.SysOrgBo;
import com.csmp.system.domain.vo.SysOrgVo;
import com.csmp.system.service.ISysDeptService;
import com.csmp.system.service.ISysOrgService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织机构管理
 * <p>
 * 组织机构 = sys_dept 中 parent_id = 0 的根部门
 *
 * @author csmp
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/org")
public class SysOrgController extends BaseController {

    private static final String EMPTY_ORG_GUIDE_MSG = "当前租户暂无组织机构，请先新增组织机构";

    private final ISysOrgService orgService;
    private final ISysDeptService deptService;

    /**
     * 获取组织机构列表
     */
    @SaCheckPermission("system:org:list")
    @GetMapping("/list")
    public R<List<SysOrgVo>> list(SysOrgBo bo) {
        List<SysOrgVo> list = orgService.selectOrgList(bo);
        if (list.isEmpty()) {
            return R.ok(EMPTY_ORG_GUIDE_MSG, list);
        }
        return R.ok(list);
    }

    /**
     * 查询组织机构下拉树结构
     */
    @SaCheckPermission("system:org:list")
    @GetMapping("/tree")
    public R<List<Tree<Long>>> tree() {
        List<Tree<Long>> tree = orgService.selectOrgTreeSelect();
        if (tree.isEmpty()) {
            return R.ok(EMPTY_ORG_GUIDE_MSG, tree);
        }
        return R.ok(tree);
    }

    /**
     * 根据组织编号获取详细信息
     *
     * @param orgId 组织ID
     */
    @SaCheckPermission("system:org:query")
    @GetMapping(value = "/{orgId}")
    public R<SysOrgVo> getInfo(@PathVariable Long orgId) {
        deptService.checkDeptDataScope(orgId);
        return R.ok(orgService.selectOrgById(orgId));
    }

    /**
     * 获取组织选择框列表
     *
     * @param orgIds 组织ID串
     */
    @SaCheckPermission("system:org:query")
    @GetMapping("/optionselect")
    public R<List<SysOrgVo>> optionselect(@RequestParam(required = false) Long[] orgIds) {
        return R.ok(orgService.selectOrgByIds(orgIds == null ? null : List.of(orgIds)));
    }

    /**
     * 获取指定组织下的部门树
     *
     * @param orgId 组织ID
     */
    @SaCheckPermission("system:org:query")
    @GetMapping("/{orgId}/dept/tree")
    public R<List<Tree<Long>>> deptTree(@PathVariable Long orgId) {
        deptService.checkDeptDataScope(orgId);
        return R.ok(deptService.selectDeptTreeByOrgId(orgId));
    }

    /**
     * 新增组织机构
     */
    @SaCheckPermission("system:org:add")
    @Log(title = "组织机构管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SysOrgBo bo) {
        if (!orgService.checkOrgNameUnique(bo)) {
            return R.fail("新增组织'" + bo.getOrgName() + "'失败，组织名称已存在");
        }
        return toAjax(orgService.insertOrg(bo));
    }

    /**
     * 修改组织机构
     */
    @SaCheckPermission("system:org:edit")
    @Log(title = "组织机构管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SysOrgBo bo) {
        deptService.checkDeptDataScope(bo.getOrgId());
        if (!orgService.checkOrgNameUnique(bo)) {
            return R.fail("修改组织'" + bo.getOrgName() + "'失败，组织名称已存在");
        }
        return toAjax(orgService.updateOrg(bo));
    }

    /**
     * 删除组织机构
     *
     * @param orgIds 组织ID数组
     */
    @SaCheckPermission("system:org:remove")
    @Log(title = "组织机构管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{orgIds}")
    public R<Void> remove(@PathVariable Long[] orgIds) {
        for (Long orgId : orgIds) {
            deptService.checkDeptDataScope(orgId);
            orgService.deleteOrgById(orgId);
        }
        return R.ok();
    }

}
