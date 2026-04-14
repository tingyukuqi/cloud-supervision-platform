package com.csmp.supply.mapper;

import com.csmp.common.mybatis.core.mapper.BaseMapperPlus;
import com.csmp.supply.domain.SupplySupplierUser;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;

/**
 * 供应商用户绑定 Mapper
 *
 * @author csmp
 */
public interface SupplySupplierUserMapper extends BaseMapperPlus<SupplySupplierUser, SupplySupplierUser> {

    /**
     * 物理删除供应商用户绑定关系
     *
     * @param ids      绑定ID列表
     * @param tenantId 租户ID
     * @return 删除条数
     */
    @Delete({
        "<script>",
        "DELETE FROM supply_supplier_user",
        "WHERE id IN",
        "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "<if test='tenantId != null and tenantId != \"\"'>",
        "AND tenant_id = #{tenantId}",
        "</if>",
        "</script>"
    })
    int deletePhysicalByIds(@Param("ids") Collection<Long> ids, @Param("tenantId") String tenantId);
}
