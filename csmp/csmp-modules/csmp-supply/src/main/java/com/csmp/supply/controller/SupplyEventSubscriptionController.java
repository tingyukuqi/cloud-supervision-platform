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
import com.csmp.supply.domain.bo.SupplyEventIngestBo;
import com.csmp.supply.domain.bo.SupplyEventLogBo;
import com.csmp.supply.domain.bo.SupplyEventSubscriptionBo;
import com.csmp.supply.domain.bo.SupplyStatusBo;
import com.csmp.supply.domain.vo.SupplyEventLogVo;
import com.csmp.supply.domain.vo.SupplyEventSubscriptionVo;
import com.csmp.supply.service.ISupplyEventSubscriptionService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * 事件订阅控制器
 *
 * @author csmp
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping({"/event-subscriptions", "/supply/event-subscriptions"})
public class SupplyEventSubscriptionController extends BaseController {

    private final ISupplyEventSubscriptionService eventSubscriptionService;

    @SaCheckPermission("supply:eventSubscription:list")
    @GetMapping("/list")
    public TableDataInfo<SupplyEventSubscriptionVo> list(SupplyEventSubscriptionBo bo, PageQuery pageQuery) {
        return eventSubscriptionService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("supply:eventSubscription:query")
    @GetMapping("/{subscriptionId}")
    public R<SupplyEventSubscriptionVo> getInfo(@PathVariable Long subscriptionId) {
        return R.ok(eventSubscriptionService.queryById(subscriptionId));
    }

    @SaCheckPermission("supply:eventSubscription:add")
    @Log(title = "事件订阅", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SupplyEventSubscriptionBo bo) {
        return toAjax(eventSubscriptionService.insertByBo(bo));
    }

    @SaCheckPermission("supply:eventSubscription:edit")
    @Log(title = "事件订阅", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SupplyEventSubscriptionBo bo) {
        return toAjax(eventSubscriptionService.updateByBo(bo));
    }

    @SaCheckPermission("supply:eventSubscription:edit")
    @Log(title = "事件订阅状态", businessType = BusinessType.UPDATE)
    @PutMapping("/{subscriptionId}/status")
    public R<Void> changeStatus(@PathVariable Long subscriptionId, @Validated @RequestBody SupplyStatusBo bo) {
        return toAjax(eventSubscriptionService.changeStatus(subscriptionId, bo.getStatus()));
    }

    @SaCheckPermission("supply:eventSubscription:remove")
    @Log(title = "事件订阅", businessType = BusinessType.DELETE)
    @DeleteMapping("/{subscriptionIds}")
    public R<Void> remove(@PathVariable @NotEmpty Long[] subscriptionIds) {
        return toAjax(eventSubscriptionService.deleteWithValidByIds(Arrays.asList(subscriptionIds), true));
    }

    @SaCheckPermission("supply:eventSubscription:list")
    @GetMapping("/{subscriptionId}/events")
    public TableDataInfo<SupplyEventLogVo> events(@PathVariable Long subscriptionId, SupplyEventLogBo bo, PageQuery pageQuery) {
        return eventSubscriptionService.queryEventPage(subscriptionId, bo, pageQuery);
    }

    @PostMapping("/{subscriptionId}/ingest")
    public R<Void> ingest(@PathVariable Long subscriptionId, @Validated @RequestBody SupplyEventIngestBo bo) {
        return toAjax(eventSubscriptionService.ingestEvent(subscriptionId, bo));
    }
}
