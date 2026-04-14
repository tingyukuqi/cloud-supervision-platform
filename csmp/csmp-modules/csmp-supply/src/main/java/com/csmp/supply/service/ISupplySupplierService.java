package com.csmp.supply.service;

import com.csmp.common.mybatis.core.page.PageQuery;
import com.csmp.common.mybatis.core.page.TableDataInfo;
import com.csmp.supply.domain.bo.SupplySupplierBo;
import com.csmp.supply.domain.bo.SupplySupplierPlatformAccountBo;
import com.csmp.supply.domain.bo.SupplySupplierUserBindBo;
import com.csmp.supply.domain.vo.SupplyOptionVo;
import com.csmp.supply.domain.vo.SupplySupplierPlatformAccountVo;
import com.csmp.supply.domain.vo.SupplySupplierUserVo;
import com.csmp.supply.domain.vo.SupplySupplierVo;
import com.csmp.system.api.domain.vo.RemoteUserVo;

import java.util.Collection;
import java.util.List;

/**
 * 供应商服务
 *
 * @author csmp
 */
public interface ISupplySupplierService {

    TableDataInfo<SupplySupplierVo> queryPageList(SupplySupplierBo bo, PageQuery pageQuery);

    SupplySupplierVo queryById(Long supplierId);

    boolean insertByBo(SupplySupplierBo bo);

    boolean updateByBo(SupplySupplierBo bo);

    boolean changeStatus(Long supplierId, String status);

    boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    List<SupplyOptionVo> queryOptions(String status);

    TableDataInfo<SupplySupplierPlatformAccountVo> queryPlatformAccountPage(Long supplierId, SupplySupplierPlatformAccountBo bo, PageQuery pageQuery);

    boolean insertPlatformAccount(SupplySupplierPlatformAccountBo bo);

    boolean updatePlatformAccount(SupplySupplierPlatformAccountBo bo);

    boolean deletePlatformAccounts(Collection<Long> ids);

    boolean bindUsers(Long supplierId, SupplySupplierUserBindBo bo);

    List<SupplySupplierUserVo> queryUserList(Long supplierId);

    List<RemoteUserVo> queryBindableUsers(Long supplierId, String keyword, Long deptId);
}
