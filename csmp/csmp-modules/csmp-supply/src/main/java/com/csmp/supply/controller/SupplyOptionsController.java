package com.csmp.supply.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.csmp.common.core.domain.R;
import com.csmp.supply.domain.vo.SupplyOptionVo;
import com.csmp.supply.service.ISupplyCloudTenantService;
import com.csmp.supply.service.ISupplyOrgCloudTenantBindService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 供应链通用选项控制器
 *
 * @author csmp
 */
@RestController
@RequiredArgsConstructor
@RequestMapping({"/options", "/supply/options"})
public class SupplyOptionsController {

    private final ISupplyOrgCloudTenantBindService bindService;
    private final ISupplyCloudTenantService cloudTenantService;

    @SaCheckPermission("supply:binding:list")
    @GetMapping("/orgs")
    public R<List<SupplyOptionVo>> orgOptions() {
        return R.ok(bindService.queryOrgOptions());
    }

    @SaCheckPermission(value = {"supply:binding:list", "supply:cloudTenant:list"}, mode = SaMode.OR)
    @GetMapping("/cloud-tenants")
    public R<List<SupplyOptionVo>> n公司(@RequestParam Long cloudPlatformId,
                                                      @RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) String bindStatus) {
        return R.ok(cloudTenantService.queryOptions(cloudPlatformId, keyword, bindStatus));
    }
}
