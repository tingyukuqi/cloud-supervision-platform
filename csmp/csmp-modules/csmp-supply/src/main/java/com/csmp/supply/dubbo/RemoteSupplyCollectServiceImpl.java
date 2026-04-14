package com.csmp.supply.dubbo;

import com.csmp.supply.api.RemoteSupplyCollectService;
import com.csmp.supply.api.domain.bo.CollectExecuteBo;
import com.csmp.supply.api.domain.vo.CollectExecuteResultVo;
import com.csmp.supply.service.ISupplyCollectConfigService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

/**
 * 供应链采集 Dubbo 服务
 *
 * @author csmp
 */
@Service
@DubboService
@RequiredArgsConstructor
public class RemoteSupplyCollectServiceImpl implements RemoteSupplyCollectService {

    private final ISupplyCollectConfigService collectConfigService;

    @Override
    public CollectExecuteResultVo executeCollect(CollectExecuteBo bo) {
        return collectConfigService.executeCollect(bo);
    }
}
