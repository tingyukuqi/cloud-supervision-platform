package com.csmp.supply.service;

import com.csmp.common.mybatis.core.page.PageQuery;
import com.csmp.common.mybatis.core.page.TableDataInfo;
import com.csmp.supply.api.domain.vo.CollectExecuteResultVo;
import com.csmp.supply.domain.bo.SupplyCloudTenantBo;
import com.csmp.supply.domain.vo.SupplyCloudTenantVo;
import com.csmp.supply.domain.vo.SupplyOptionVo;

import java.util.List;

/**
 * 云租户服务
 *
 * @author csmp
 */
public interface ISupplyCloudTenantService {

    TableDataInfo<SupplyCloudTenantVo> queryPageList(SupplyCloudTenantBo bo, PageQuery pageQuery);

    CollectExecuteResultVo refreshByCloudPlatformId(Long cloudPlatformId, Long triggerUserId);

    List<SupplyOptionVo> queryOptions(Long cloudPlatformId, String keyword, String bindStatus);
}
