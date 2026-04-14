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
import com.csmp.supply.domain.bo.SupplyPhysicalResourceBo;
import com.csmp.supply.domain.vo.SupplyImportResultVo;
import com.csmp.supply.domain.vo.SupplyPhysicalResourceImportVo;
import com.csmp.supply.domain.vo.SupplyPhysicalResourceVo;
import com.csmp.supply.service.ISupplyPhysicalResourceService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 物理资源控制器
 *
 * @author csmp
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping({"/physical-resources", "/supply/physical-resources"})
public class SupplyPhysicalResourceController extends BaseController {

    private final ISupplyPhysicalResourceService physicalResourceService;

    @SaCheckPermission("supply:physicalResource:list")
    @GetMapping("/list")
    public TableDataInfo<SupplyPhysicalResourceVo> list(SupplyPhysicalResourceBo bo, PageQuery pageQuery) {
        return physicalResourceService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("supply:physicalResource:query")
    @GetMapping("/{resourceId}")
    public R<SupplyPhysicalResourceVo> getInfo(@NotNull(message = "资源ID不能为空") @PathVariable Long resourceId) {
        return R.ok(physicalResourceService.queryById(resourceId));
    }

    @SaCheckPermission("supply:physicalResource:add")
    @Log(title = "物理资源", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SupplyPhysicalResourceBo bo) {
        return toAjax(physicalResourceService.insertByBo(bo));
    }

    @SaCheckPermission("supply:physicalResource:edit")
    @Log(title = "物理资源", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SupplyPhysicalResourceBo bo) {
        return toAjax(physicalResourceService.updateByBo(bo));
    }

    @SaCheckPermission("supply:physicalResource:remove")
    @Log(title = "物理资源", businessType = BusinessType.DELETE)
    @DeleteMapping("/{resourceIds}")
    public R<Void> remove(@PathVariable @NotEmpty Long[] resourceIds) {
        return toAjax(physicalResourceService.deleteWithValidByIds(Arrays.asList(resourceIds), true));
    }

    @SaCheckPermission("supply:physicalResource:export")
    @Log(title = "物理资源", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(SupplyPhysicalResourceBo bo, HttpServletResponse response) {
        ExcelUtil.exportExcel(physicalResourceService.queryList(bo), "物理资源", SupplyPhysicalResourceVo.class, response);
    }

    @SaCheckPermission("supply:physicalResource:import")
    @Log(title = "物理资源", businessType = BusinessType.IMPORT)
    @PostMapping(value = "/importData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<SupplyImportResultVo> importData(@RequestPart("file") MultipartFile file, @RequestParam boolean updateSupport) throws Exception {
        return R.ok(physicalResourceService.importData(file.getInputStream(), updateSupport));
    }

    @SaCheckPermission("supply:physicalResource:import")
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtil.exportExcel(new ArrayList<>(), "物理资源导入模板", SupplyPhysicalResourceImportVo.class, response);
    }
}
