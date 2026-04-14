package com.csmp.supply.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.csmp.common.core.domain.R;
import com.csmp.common.core.validate.AddGroup;
import com.csmp.common.core.validate.EditGroup;
import com.csmp.common.log.annotation.Log;
import com.csmp.common.log.enums.BusinessType;
import com.csmp.common.mybatis.core.page.PageQuery;
import com.csmp.common.mybatis.core.page.TableDataInfo;
import com.csmp.common.web.core.BaseController;
import com.csmp.supply.domain.bo.SupplyOrgCloudTenantBindBo;
import com.csmp.supply.domain.vo.SupplyOrgCloudTenantBindVo;
import com.csmp.supply.service.ISupplyOrgCloudTenantBindService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * 组织云租户绑定控制器
 *
 * @author csmp
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping({"/org-tenant-bindings", "/supply/org-tenant-bindings"})
public class SupplyOrgCloudTenantBindController extends BaseController {

    private final ISupplyOrgCloudTenantBindService bindService;

    @SaCheckPermission("supply:binding:list")
    @GetMapping("/list")
    public TableDataInfo<SupplyOrgCloudTenantBindVo> list(SupplyOrgCloudTenantBindBo bo, PageQuery pageQuery) {
        return bindService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("supply:binding:query")
    @GetMapping("/{bindingId}")
    public R<SupplyOrgCloudTenantBindVo> getInfo(@PathVariable Long bindingId) {
        return R.ok(bindService.queryById(bindingId));
    }

    @SaCheckPermission("supply:binding:add")
    @Log(title = "组织云租户绑定", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SupplyOrgCloudTenantBindBo bo) {
        return toAjax(bindService.insertByBo(bo));
    }

    @SaCheckPermission("supply:binding:edit")
    @Log(title = "组织云租户绑定", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SupplyOrgCloudTenantBindBo bo) {
        return toAjax(bindService.updateByBo(bo));
    }

    @SaCheckPermission("supply:binding:remove")
    @Log(title = "组织云租户绑定", businessType = BusinessType.DELETE)
    @DeleteMapping("/{bindingIds}")
    public R<Void> remove(@PathVariable @NotEmpty Long[] bindingIds) {
        return toAjax(bindService.deleteWithValidByIds(Arrays.asList(bindingIds), true));
    }
}
