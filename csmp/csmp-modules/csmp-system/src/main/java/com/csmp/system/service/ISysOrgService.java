package com.csmp.system.service;

import cn.hutool.core.lang.tree.Tree;
import com.csmp.system.domain.bo.SysOrgBo;
import com.csmp.system.domain.vo.SysOrgVo;

import java.util.List;

/**
 * 组织机构管理 服务层
 * <p>
 * 组织机构 = sys_dept 中 parent_id = 0 的根部门
 *
 * @author csmp
 */
public interface ISysOrgService {

    /**
     * 查询组织机构列表
     *
     * @param bo 查询条件
     * @return 组织机构列表
     */
    List<SysOrgVo> selectOrgList(SysOrgBo bo);

    /**
     * 查询组织机构树结构
     *
     * @return 组织机构下拉树
     */
    List<Tree<Long>> selectOrgTreeSelect();

    /**
     * 根据组织ID查询信息
     *
     * @param orgId 组织ID
     * @return 组织机构信息
     */
    SysOrgVo selectOrgById(Long orgId);

    /**
     * 通过组织ID串查询组织
     *
     * @param orgIds 组织ID列表
     * @return 组织机构列表
     */
    List<SysOrgVo> selectOrgByIds(List<Long> orgIds);

    /**
     * 校验组织名称是否唯一
     *
     * @param bo 组织信息
     * @return 结果 true 唯一 false 不唯一
     */
    boolean checkOrgNameUnique(SysOrgBo bo);

    /**
     * 新增组织机构
     *
     * @param bo 组织信息
     * @return 结果
     */
    int insertOrg(SysOrgBo bo);

    /**
     * 修改组织机构
     *
     * @param bo 组织信息
     * @return 结果
     */
    int updateOrg(SysOrgBo bo);

    /**
     * 删除组织机构
     *
     * @param orgId 组织ID
     * @return 结果
     */
    int deleteOrgById(Long orgId);

}
