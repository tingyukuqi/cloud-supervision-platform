package com.csmp.supply.service;

import com.csmp.common.mybatis.core.page.PageQuery;
import com.csmp.common.mybatis.core.page.TableDataInfo;
import com.csmp.supply.domain.bo.SupplyEventIngestBo;
import com.csmp.supply.domain.bo.SupplyEventLogBo;
import com.csmp.supply.domain.bo.SupplyEventSubscriptionBo;
import com.csmp.supply.domain.vo.SupplyEventLogVo;
import com.csmp.supply.domain.vo.SupplyEventSubscriptionVo;

import java.util.Collection;

/**
 * 事件订阅服务
 *
 * @author csmp
 */
public interface ISupplyEventSubscriptionService {

    TableDataInfo<SupplyEventSubscriptionVo> queryPageList(SupplyEventSubscriptionBo bo, PageQuery pageQuery);

    SupplyEventSubscriptionVo queryById(Long subscriptionId);

    boolean insertByBo(SupplyEventSubscriptionBo bo);

    boolean updateByBo(SupplyEventSubscriptionBo bo);

    boolean changeStatus(Long subscriptionId, String status);

    boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    TableDataInfo<SupplyEventLogVo> queryEventPage(Long subscriptionId, SupplyEventLogBo bo, PageQuery pageQuery);

    boolean ingestEvent(Long subscriptionId, SupplyEventIngestBo bo);
}
