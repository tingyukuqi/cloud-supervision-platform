package com.csmp.supply.service;

import com.csmp.common.mybatis.core.page.PageQuery;
import com.csmp.common.mybatis.core.page.TableDataInfo;
import com.csmp.supply.domain.bo.SupplyCloudPlatformBo;
import com.csmp.supply.domain.vo.SupplyCloudPlatformVo;
import com.csmp.supply.domain.vo.SupplyOptionVo;

import java.util.Collection;
import java.util.List;

/**
 * 云平台服务
 *
 * @author csmp
 */
public interface ISupplyCloudPlatformService {

    TableDataInfo<SupplyCloudPlatformVo> queryPageList(SupplyCloudPlatformBo bo, PageQuery pageQuery);

    List<SupplyCloudPlatformVo> queryList(SupplyCloudPlatformBo bo);

    SupplyCloudPlatformVo queryById(Long platformId);

    boolean insertByBo(SupplyCloudPlatformBo bo);

    boolean updateByBo(SupplyCloudPlatformBo bo);

    boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    List<SupplyOptionVo> queryOptions(String providerCode, String status);
}
