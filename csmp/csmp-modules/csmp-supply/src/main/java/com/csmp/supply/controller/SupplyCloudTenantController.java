package com.csmp.supply.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.csmp.common.core.domain.R;
import com.csmp.common.mybatis.core.page.PageQuery;
import com.csmp.common.mybatis.core.page.TableDataInfo;
import com.csmp.common.satoken.utils.LoginHelper;
import com.csmp.common.web.core.BaseController;
import com.csmp.supply.api.domain.vo.CollectExecuteResultVo;
import com.csmp.supply.domain.bo.SupplyCloudTenantBo;
import com.csmp.supply.domain.vo.SupplyCloudTenantVo;
import com.csmp.supply.service.ISupplyCloudTenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 云租户控制器
 *
 * @author csmp
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping({"/cloud-tenants", "/supply/cloud-tenants"})
public class SupplyCloudTenantController extends BaseController {

    private final ISupplyCloudTenantService cloudTenantService;

    @SaCheckPermission("supply:cloudTenant:list")
    @GetMapping("/list")
    public TableDataInfo<SupplyCloudTenantVo> list(SupplyCloudTenantBo bo, PageQuery pageQuery) {
        return cloudTenantService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("supply:cloudTenant:refresh")
    @PostMapping("/refresh/{cloudPlatformId}")
    public R<CollectExecuteResultVo> refresh(@PathVariable Long cloudPlatformId) {
        return R.ok(cloudTenantService.refreshByCloudPlatformId(cloudPlatformId, LoginHelper.getUserId()));
    }
}
