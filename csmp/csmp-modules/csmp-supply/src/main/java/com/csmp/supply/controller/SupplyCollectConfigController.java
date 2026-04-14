package com.csmp.supply.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.csmp.common.core.domain.R;
import com.csmp.common.core.validate.AddGroup;
import com.csmp.common.core.validate.EditGroup;
import com.csmp.common.log.annotation.Log;
import com.csmp.common.log.enums.BusinessType;
import com.csmp.common.mybatis.core.page.PageQuery;
import com.csmp.common.mybatis.core.page.TableDataInfo;
import com.csmp.common.satoken.utils.LoginHelper;
import com.csmp.common.web.core.BaseController;
import com.csmp.supply.api.domain.bo.CollectExecuteBo;
import com.csmp.supply.api.domain.vo.CollectExecuteResultVo;
import com.csmp.supply.domain.bo.SupplyCollectConfigBo;
import com.csmp.supply.domain.bo.SupplyCollectExecuteRequestBo;
import com.csmp.supply.domain.bo.SupplyCollectLogBo;
import com.csmp.supply.domain.bo.SupplyStatusBo;
import com.csmp.supply.domain.vo.SupplyCollectConfigVo;
import com.csmp.supply.domain.vo.SupplyCollectLogVo;
import com.csmp.supply.service.ISupplyCollectConfigService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * 采集配置控制器
 *
 * @author csmp
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping({"/collect-configs", "/supply/collect-configs"})
public class SupplyCollectConfigController extends BaseController {

    private final ISupplyCollectConfigService collectConfigService;

    @SaCheckPermission("supply:collectConfig:list")
    @GetMapping("/list")
    public TableDataInfo<SupplyCollectConfigVo> list(SupplyCollectConfigBo bo, PageQuery pageQuery) {
        return collectConfigService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("supply:collectConfig:query")
    @GetMapping("/{collectConfigId}")
    public R<SupplyCollectConfigVo> getInfo(@PathVariable Long collectConfigId) {
        return R.ok(collectConfigService.queryById(collectConfigId));
    }

    @SaCheckPermission("supply:collectConfig:add")
    @Log(title = "采集配置", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SupplyCollectConfigBo bo) {
        return toAjax(collectConfigService.insertByBo(bo));
    }

    @SaCheckPermission("supply:collectConfig:edit")
    @Log(title = "采集配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SupplyCollectConfigBo bo) {
        return toAjax(collectConfigService.updateByBo(bo));
    }

    @SaCheckPermission("supply:collectConfig:edit")
    @Log(title = "采集配置状态", businessType = BusinessType.UPDATE)
    @PutMapping("/{collectConfigId}/status")
    public R<Void> changeStatus(@PathVariable Long collectConfigId, @Validated @RequestBody SupplyStatusBo bo) {
        return toAjax(collectConfigService.changeStatus(collectConfigId, bo.getStatus()));
    }

    @SaCheckPermission("supply:collectConfig:remove")
    @Log(title = "采集配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{collectConfigIds}")
    public R<Void> remove(@PathVariable @NotEmpty Long[] collectConfigIds) {
        return toAjax(collectConfigService.deleteWithValidByIds(Arrays.asList(collectConfigIds), true));
    }

    @SaCheckPermission("supply:collectConfig:execute")
    @Log(title = "采集配置执行", businessType = BusinessType.OTHER)
    @PostMapping("/{collectConfigId}/execute")
    public R<CollectExecuteResultVo> execute(@PathVariable Long collectConfigId,
                                             @Validated @RequestBody SupplyCollectExecuteRequestBo requestBo) {
        CollectExecuteBo bo = new CollectExecuteBo();
        bo.setCollectConfigId(collectConfigId);
        bo.setExecuteMode(requestBo.getExecuteMode());
        bo.setOperatorRemark(requestBo.getOperatorRemark());
        bo.setTriggerUserId(LoginHelper.getUserId());
        bo.setTenantId(LoginHelper.getTenantId());
        return R.ok(collectConfigService.executeCollect(bo));
    }

    @SaCheckPermission("supply:collectConfig:list")
    @GetMapping("/{collectConfigId}/logs")
    public TableDataInfo<SupplyCollectLogVo> logs(@PathVariable Long collectConfigId, SupplyCollectLogBo bo, PageQuery pageQuery) {
        return collectConfigService.queryLogPage(collectConfigId, bo, pageQuery);
    }
}
