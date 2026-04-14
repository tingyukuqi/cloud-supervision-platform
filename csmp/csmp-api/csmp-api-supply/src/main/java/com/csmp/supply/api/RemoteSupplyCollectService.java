package com.csmp.supply.api;

import com.csmp.supply.api.domain.bo.CollectExecuteBo;
import com.csmp.supply.api.domain.vo.CollectExecuteResultVo;

/**
 * 供应链采集执行远程服务
 *
 * @author csmp
 */
public interface RemoteSupplyCollectService {

    /**
     * 执行采集任务
     *
     * @param bo 执行参数
     * @return 执行结果
     */
    CollectExecuteResultVo executeCollect(CollectExecuteBo bo);
}
