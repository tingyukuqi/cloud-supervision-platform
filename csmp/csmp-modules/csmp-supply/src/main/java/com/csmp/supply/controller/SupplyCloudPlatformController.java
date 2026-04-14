package com.csmp.supply.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.csmp.common.core.domain.R;
import com.csmp.common.core.validate.AddGroup;
import com.csmp.common.core.validate.EditGroup;
import com.csmp.common.excel.utils.ExcelUtil;
import com.csmp.common.log.annotation.Log;
import com.csmp.common.log.enums.BusinessType;
import com.csmp.common.mybatis.core.page.PageQuery;
import com.csmp.common.mybatis.core.page.TableDataInfo;
import com.csmp.common.web.core.BaseController;
import com.csmp.supply.domain.bo.SupplyCloudPlatformBo;
import com.csmp.supply.domain.vo.SupplyCloudPlatformVo;
import com.csmp.supply.domain.vo.SupplyOptionVo;
import com.csmp.supply.service.ISupplyCloudPlatformService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 云平台控制器
 *
 * @author csmp
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping({"/cloud-platforms", "/supply/cloud-platforms"})
public class SupplyCloudPlatformController extends BaseController {

    private final ISupplyCloudPlatformService cloudPlatformService;

    @SaCheckPermission("supply:cloudPlatform:list")
    @GetMapping("/list")
    public TableDataInfo<SupplyCloudPlatformVo> list(SupplyCloudPlatformBo bo, PageQuery pageQuery) {
        return cloudPlatformService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("supply:cloudPlatform:query")
    @GetMapping("/{platformId}")
    public R<SupplyCloudPlatformVo> getInfo(@NotNull @PathVariable Long platformId) {
        return R.ok(cloudPlatformService.queryById(platformId));
    }

    @SaCheckPermission("supply:cloudPlatform:add")
    @Log(title = "云平台", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SupplyCloudPlatformBo bo) {
        return toAjax(cloudPlatformService.insertByBo(bo));
    }

    @SaCheckPermission("supply:cloudPlatform:edit")
    @Log(title = "云平台", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SupplyCloudPlatformBo bo) {
        return toAjax(cloudPlatformService.updateByBo(bo));
    }

    @SaCheckPermission("supply:cloudPlatform:remove")
    @Log(title = "云平台", businessType = BusinessType.DELETE)
    @DeleteMapping("/{platformIds}")
    public R<Void> remove(@PathVariable @NotEmpty Long[] platformIds) {
        return toAjax(cloudPlatformService.deleteWithValidByIds(Arrays.asList(platformIds), true));
    }

    @SaCheckPermission("supply:cloudPlatform:list")
    @GetMapping("/options")
    public R<List<SupplyOptionVo>> options(@RequestParam(required = false) String providerCode,
                                           @RequestParam(required = false) String status) {
        return R.ok(cloudPlatformService.queryOptions(providerCode, status));
    }

    @SaCheckPermission("supply:cloudPlatform:export")
    @Log(title = "云平台", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(SupplyCloudPlatformBo bo, HttpServletResponse response) {
        ExcelUtil.exportExcel(cloudPlatformService.queryList(bo), "云平台", SupplyCloudPlatformVo.class, response);
    }
}
