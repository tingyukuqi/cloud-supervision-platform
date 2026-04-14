package com.csmp.auth.domain.convert;

import io.github.linpeilie.BaseMapper;
import com.csmp.auth.domain.vo.TenantListVo;
import com.csmp.system.api.domain.vo.RemoteTenantVo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * 租户vo转换器
 * @author zhujie
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TenantVoConvert extends BaseMapper<RemoteTenantVo, TenantListVo> {

}
