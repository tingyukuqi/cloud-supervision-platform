package com.csmp.supply.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.csmp.common.core.domain.R;
import com.csmp.common.core.validate.AddGroup;
import com.csmp.common.core.validate.EditGroup;
import com.csmp.common.core.validate.QueryGroup;
import com.csmp.common.idempotent.annotation.RepeatSubmit;
import com.csmp.common.log.annotation.Log;
import com.csmp.common.log.enums.BusinessType;
import com.csmp.common.mybatis.core.page.PageQuery;
import com.csmp.common.mybatis.core.page.TableDataInfo;
import com.csmp.common.web.core.BaseController;
import com.csmp.supply.domain.bo.SupplyStatusBo;
import com.csmp.supply.domain.bo.SupplySupplierBo;
import com.csmp.supply.domain.bo.SupplySupplierPlatformAccountBo;
import com.csmp.supply.domain.bo.SupplySupplierUserBindBo;
import com.csmp.supply.domain.vo.SupplyOptionVo;
import com.csmp.supply.domain.vo.SupplySupplierPlatformAccountVo;
import com.csmp.supply.domain.vo.SupplySupplierUserVo;
import com.csmp.supply.domain.vo.SupplySupplierVo;
import com.csmp.supply.service.ISupplySupplierService;
import com.csmp.system.api.domain.vo.RemoteUserVo;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 供应商控制器
 *
 * @author csmp
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping({"/suppliers", "/supply/suppliers"})
public class SupplySupplierController extends BaseController {

    private final ISupplySupplierService supplierService;

    @SaCheckPermission("supply:supplier:list")
    @GetMapping("/list")
    public TableDataInfo<SupplySupplierVo> list(@Validated(QueryGroup.class) SupplySupplierBo bo, PageQuery pageQuery) {
        return supplierService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("supply:supplier:query")
    @GetMapping("/{supplierId}")
    public R<SupplySupplierVo> getInfo(@NotNull(message = "供应商ID不能为空") @PathVariable Long supplierId) {
        return R.ok(supplierService.queryById(supplierId));
    }

    @SaCheckPermission("supply:supplier:add")
    @Log(title = "供应商", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SupplySupplierBo bo) {
        return toAjax(supplierService.insertByBo(bo));
    }

    @SaCheckPermission("supply:supplier:edit")
    @Log(title = "供应商", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SupplySupplierBo bo) {
        return toAjax(supplierService.updateByBo(bo));
    }

    @SaCheckPermission("supply:supplier:edit")
    @Log(title = "供应商状态", businessType = BusinessType.UPDATE)
    @PutMapping("/{supplierId}/status")
    public R<Void> changeStatus(@PathVariable Long supplierId, @Validated @RequestBody SupplyStatusBo bo) {
        return toAjax(supplierService.changeStatus(supplierId, bo.getStatus()));
    }

    @SaCheckPermission("supply:supplier:remove")
    @Log(title = "供应商", businessType = BusinessType.DELETE)
    @DeleteMapping("/{supplierIds}")
    public R<Void> remove(@NotEmpty(message = "供应商ID不能为空") @PathVariable Long[] supplierIds) {
        return toAjax(supplierService.deleteWithValidByIds(Arrays.asList(supplierIds), true));
    }

    @SaCheckPermission("supply:supplier:list")
    @GetMapping("/options")
    public R<List<SupplyOptionVo>> options(@RequestParam(required = false) String status) {
        return R.ok(supplierService.queryOptions(status));
    }

    @SaCheckPermission("supply:supplier:list")
    @GetMapping("/{supplierId}/platform-accounts")
    public TableDataInfo<SupplySupplierPlatformAccountVo> accountList(@PathVariable Long supplierId,
                                                                      SupplySupplierPlatformAccountBo bo,
                                                                      PageQuery pageQuery) {
        return supplierService.queryPlatformAccountPage(supplierId, bo, pageQuery);
    }

    @SaCheckPermission("supply:supplier:edit")
    @Log(title = "供应商平台账号", businessType = BusinessType.INSERT)
    @PostMapping("/{supplierId}/platform-accounts")
    public R<Void> addAccount(@PathVariable Long supplierId, @Validated(AddGroup.class) @RequestBody SupplySupplierPlatformAccountBo bo) {
        bo.setSupplierId(supplierId);
        return toAjax(supplierService.insertPlatformAccount(bo));
    }

    @SaCheckPermission("supply:supplier:edit")
    @Log(title = "供应商平台账号", businessType = BusinessType.UPDATE)
    @PutMapping("/platform-accounts")
    public R<Void> editAccount(@Validated(EditGroup.class) @RequestBody SupplySupplierPlatformAccountBo bo) {
        return toAjax(supplierService.updatePlatformAccount(bo));
    }

    @SaCheckPermission("supply:supplier:remove")
    @Log(title = "供应商平台账号", businessType = BusinessType.DELETE)
    @DeleteMapping("/platform-accounts/{accountIds}")
    public R<Void> removeAccount(@PathVariable Long[] accountIds) {
        return toAjax(supplierService.deletePlatformAccounts(Arrays.asList(accountIds)));
    }

    @SaCheckPermission("supply:supplier:edit")
    @Log(title = "供应商用户绑定", businessType = BusinessType.UPDATE)
    @PutMapping("/{supplierId}/users")
    public R<Void> bindUsers(@PathVariable Long supplierId, @Validated @RequestBody SupplySupplierUserBindBo bo) {
        return toAjax(supplierService.bindUsers(supplierId, bo));
    }

    @SaCheckPermission("supply:supplier:edit")
    @GetMapping("/{supplierId}/bindable-users")
    public R<List<RemoteUserVo>> bindableUsers(@PathVariable Long supplierId,
                                               @RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) Long deptId) {
        return R.ok(supplierService.queryBindableUsers(supplierId, keyword, deptId));
    }

    @SaCheckPermission("supply:supplier:list")
    @GetMapping("/{supplierId}/users")
    public R<List<SupplySupplierUserVo>> userList(@PathVariable Long supplierId) {
        return R.ok(supplierService.queryUserList(supplierId));
    }
}
