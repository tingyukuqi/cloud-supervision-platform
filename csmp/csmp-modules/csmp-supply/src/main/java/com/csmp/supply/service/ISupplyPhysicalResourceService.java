package com.csmp.supply.service;

import com.csmp.common.mybatis.core.page.PageQuery;
import com.csmp.common.mybatis.core.page.TableDataInfo;
import com.csmp.supply.domain.bo.SupplyPhysicalResourceBo;
import com.csmp.supply.domain.vo.SupplyImportResultVo;
import com.csmp.supply.domain.vo.SupplyPhysicalResourceVo;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

/**
 * 物理资源服务
 *
 * @author csmp
 */
public interface ISupplyPhysicalResourceService {

    TableDataInfo<SupplyPhysicalResourceVo> queryPageList(SupplyPhysicalResourceBo bo, PageQuery pageQuery);

    List<SupplyPhysicalResourceVo> queryList(SupplyPhysicalResourceBo bo);

    SupplyPhysicalResourceVo queryById(Long resourceId);

    boolean insertByBo(SupplyPhysicalResourceBo bo);

    boolean updateByBo(SupplyPhysicalResourceBo bo);

    boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    SupplyImportResultVo importData(InputStream inputStream, boolean updateSupport) throws Exception;
}
