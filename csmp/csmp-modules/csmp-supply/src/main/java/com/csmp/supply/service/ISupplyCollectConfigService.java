package com.csmp.supply.service;

import com.csmp.common.mybatis.core.page.PageQuery;
import com.csmp.common.mybatis.core.page.TableDataInfo;
import com.csmp.supply.api.domain.bo.CollectExecuteBo;
import com.csmp.supply.api.domain.vo.CollectExecuteResultVo;
import com.csmp.supply.domain.bo.SupplyCollectConfigBo;
import com.csmp.supply.domain.bo.SupplyCollectLogBo;
import com.csmp.supply.domain.vo.SupplyCollectConfigVo;
import com.csmp.supply.domain.vo.SupplyCollectLogVo;

import java.util.Collection;

/**
 * 采集配置服务
 *
 * @author csmp
 */
public interface ISupplyCollectConfigService {

    TableDataInfo<SupplyCollectConfigVo> queryPageList(SupplyCollectConfigBo bo, PageQuery pageQuery);

    SupplyCollectConfigVo queryById(Long collectConfigId);

    boolean insertByBo(SupplyCollectConfigBo bo);

    boolean updateByBo(SupplyCollectConfigBo bo);

    boolean changeStatus(Long collectConfigId, String status);

    boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    CollectExecuteResultVo executeCollect(CollectExecuteBo bo);

    TableDataInfo<SupplyCollectLogVo> queryLogPage(Long collectConfigId, SupplyCollectLogBo bo, PageQuery pageQuery);
}
