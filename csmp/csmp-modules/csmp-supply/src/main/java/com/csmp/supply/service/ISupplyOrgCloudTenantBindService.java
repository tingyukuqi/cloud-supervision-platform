package com.csmp.supply.service;

import com.csmp.common.mybatis.core.page.PageQuery;
import com.csmp.common.mybatis.core.page.TableDataInfo;
import com.csmp.supply.domain.bo.SupplyOrgCloudTenantBindBo;
import com.csmp.supply.domain.vo.SupplyOptionVo;
import com.csmp.supply.domain.vo.SupplyOrgCloudTenantBindVo;

import java.util.Collection;
import java.util.List;

/**
 * 组织云租户绑定服务
 *
 * @author csmp
 */
public interface ISupplyOrgCloudTenantBindService {

    TableDataInfo<SupplyOrgCloudTenantBindVo> queryPageList(SupplyOrgCloudTenantBindBo bo, PageQuery pageQuery);

    SupplyOrgCloudTenantBindVo queryById(Long bindingId);

    boolean insertByBo(SupplyOrgCloudTenantBindBo bo);

    boolean updateByBo(SupplyOrgCloudTenantBindBo bo);

    boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    List<SupplyOptionVo> queryOrgOptions();
}
