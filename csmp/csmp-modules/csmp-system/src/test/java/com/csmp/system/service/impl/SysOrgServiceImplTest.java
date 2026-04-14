package com.csmp.system.service.impl;

import com.csmp.common.core.constant.CacheNames;
import com.csmp.common.core.constant.TenantConstants;
import com.csmp.common.core.exception.ServiceException;
import com.csmp.system.api.RemoteTenantService;
import com.csmp.system.api.domain.vo.RemoteTenantVo;
import com.csmp.system.domain.SysDept;
import com.csmp.system.domain.SysPost;
import com.csmp.system.domain.bo.SysOrgBo;
import com.csmp.system.domain.vo.SysDeptVo;
import com.csmp.system.domain.vo.SysOrgVo;
import com.csmp.system.mapper.SysDeptMapper;
import com.csmp.system.mapper.SysPostMapper;
import com.csmp.system.mapper.SysUserMapper;
import com.csmp.system.service.ISysPostService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("dev")
class SysOrgServiceImplTest {

    @Mock
    private SysDeptMapper deptMapper;
    @Mock
    private SysUserMapper userMapper;
    @Mock
    private SysPostMapper postMapper;
    @Mock
    private ISysPostService postService;
    @Mock
    private RemoteTenantService remoteTenantService;

    @InjectMocks
    private SysOrgServiceImpl orgService;

    @Test
    void selectOrgListShouldUseDataPermissionAwareDeptQuery() {
        SysDeptVo org = new SysDeptVo();
        org.setDeptId(100L);
        org.setTenantId("000123");
        org.setDeptName("总部");
        RemoteTenantVo tenant = new RemoteTenantVo();
        tenant.setTenantId("000123");
        tenant.setCompanyName("华东租户");
        when(deptMapper.selectDeptList(any())).thenReturn(List.of(org));
        when(remoteTenantService.queryList()).thenReturn(List.of(tenant));

        List<SysOrgVo> result = orgService.selectOrgList(new SysOrgBo());

        verify(deptMapper).selectDeptList(any());
        verify(remoteTenantService).queryList();
        verify(deptMapper, never()).selectVoList(any());
        Assertions.assertEquals("000123", result.get(0).getTenantId());
        Assertions.assertEquals("华东租户", result.get(0).getTenantName());
    }

    @Test
    void selectOrgTreeSelectShouldUseDataPermissionAwareDeptQuery() {
        SysDeptVo org = new SysDeptVo();
        org.setDeptId(100L);
        org.setParentId(0L);
        org.setDeptName("总部");
        when(deptMapper.selectDeptList(any())).thenReturn(List.of(org));

        orgService.selectOrgTreeSelect();

        verify(deptMapper).selectDeptList(any());
        verify(deptMapper, never()).selectVoList(any());
    }

    @Test
    void deleteOrgByIdShouldRejectPostsInChildDepartments() {
        when(deptMapper.exists(any())).thenReturn(false);
        when(userMapper.exists(any())).thenReturn(false);
        when(deptMapper.selectDeptAndChildById(100L)).thenReturn(List.of(100L, 101L));
        when(postService.countPostByDeptId(100L)).thenReturn(0L);
        when(postService.countPostByDeptId(101L)).thenReturn(1L);

        assertThrows(ServiceException.class, () -> orgService.deleteOrgById(100L));
    }

    @Test
    void insertOrgShouldBindSelectedTenantIdForPlatformTenant() {
        SysOrgBo bo = new SysOrgBo();
        bo.setTenantId("000123");
        bo.setOrgName("华东区");
        bo.setOrderNum(1);
        bo.setStatus("0");

        SysOrgServiceImpl service = spy(orgService);
        doReturn(TenantConstants.DEFAULT_TENANT_ID).when(service).loginTenantId();
        doReturn(TenantConstants.DEFAULT_TENANT_ID).when(service).dataTenantId();

        service.insertOrg(bo);

        verify(deptMapper).insert(argThat((SysDept dept) ->
            "000123".equals(dept.getTenantId())
                && "华东区".equals(dept.getDeptName())
                && Long.valueOf(0L).equals(dept.getParentId())
        ));
    }

    @Test
    void insertOrgShouldRejectCrossTenantCreationForNormalTenant() {
        SysOrgBo bo = new SysOrgBo();
        bo.setTenantId("000999");
        bo.setOrgName("华东区");
        bo.setOrderNum(1);
        bo.setStatus("0");

        SysOrgServiceImpl service = spy(orgService);
        doReturn("000123").when(service).loginTenantId();

        assertThrows(ServiceException.class, () -> service.insertOrg(bo));

        verify(deptMapper, never()).insert(any(SysDept.class));
    }

    @Test
    void updateOrgShouldMoveTenantForPlatformTenant() {
        SysOrgBo bo = new SysOrgBo();
        bo.setOrgId(100L);
        bo.setTenantId("000456");
        bo.setOrgName("华东总部");
        bo.setOrderNum(1);
        bo.setStatus("0");

        SysDept existing = new SysDept();
        existing.setDeptId(100L);
        existing.setTenantId("000123");
        existing.setParentId(0L);
        when(deptMapper.selectById(100L)).thenReturn(existing);
        when(deptMapper.selectDeptAndChildById(100L)).thenReturn(List.of(100L, 101L));
        when(userMapper.exists(any())).thenReturn(false);

        SysOrgServiceImpl service = spy(orgService);
        doReturn(TenantConstants.DEFAULT_TENANT_ID).when(service).loginTenantId();

        service.updateOrg(bo);

        verify(deptMapper).updateById(argThat((SysDept dept) ->
            Long.valueOf(100L).equals(dept.getDeptId())
                && "000456".equals(dept.getTenantId())
                && "华东总部".equals(dept.getDeptName())
        ));
        verify(deptMapper).update(org.mockito.ArgumentMatchers.<SysDept>isNull(), any());
        verify(postMapper).update(org.mockito.ArgumentMatchers.<SysPost>isNull(), any());
    }

    @Test
    void updateOrgShouldRejectTenantTransferWhenUsersExist() {
        SysOrgBo bo = new SysOrgBo();
        bo.setOrgId(100L);
        bo.setTenantId("000456");
        bo.setOrgName("华东总部");
        bo.setOrderNum(1);
        bo.setStatus("0");

        SysDept existing = new SysDept();
        existing.setDeptId(100L);
        existing.setTenantId("000123");
        existing.setParentId(0L);
        when(deptMapper.selectById(100L)).thenReturn(existing);
        when(deptMapper.selectDeptAndChildById(100L)).thenReturn(List.of(100L, 101L));
        when(userMapper.exists(any())).thenReturn(true);

        SysOrgServiceImpl service = spy(orgService);
        doReturn(TenantConstants.DEFAULT_TENANT_ID).when(service).loginTenantId();

        assertThrows(ServiceException.class, () -> service.updateOrg(bo));
    }

    @Test
    void orgDetailCacheShouldNotReuseDeptCache() throws NoSuchMethodException {
        Method selectOrgById = SysOrgServiceImpl.class.getMethod("selectOrgById", Long.class);
        Cacheable cacheable = selectOrgById.getAnnotation(Cacheable.class);

        Assertions.assertNotNull(cacheable);
        Assertions.assertArrayEquals(new String[]{CacheNames.SYS_ORG}, cacheable.cacheNames());

        Method updateOrg = SysOrgServiceImpl.class.getMethod("updateOrg", SysOrgBo.class);
        Caching updateCaching = updateOrg.getAnnotation(Caching.class);
        Assertions.assertNotNull(updateCaching);
        Assertions.assertEquals(CacheNames.SYS_ORG, updateCaching.evict()[0].cacheNames()[0]);

        Method deleteOrgById = SysOrgServiceImpl.class.getMethod("deleteOrgById", Long.class);
        Caching deleteCaching = deleteOrgById.getAnnotation(Caching.class);
        Assertions.assertNotNull(deleteCaching);
        Assertions.assertEquals(CacheNames.SYS_ORG, deleteCaching.evict()[0].cacheNames()[0]);
    }

    @Test
    void selectOrgByIdShouldPopulateTenantName() {
        SysDeptVo org = new SysDeptVo();
        org.setDeptId(100L);
        org.setParentId(0L);
        org.setTenantId("000123");
        org.setDeptName("总部");
        RemoteTenantVo tenant = new RemoteTenantVo();
        tenant.setTenantId("000123");
        tenant.setCompanyName("华东租户");
        when(deptMapper.selectDeptList(any())).thenReturn(List.of(org));
        when(remoteTenantService.queryList()).thenReturn(List.of(tenant));

        SysOrgVo result = orgService.selectOrgById(100L);

        Assertions.assertEquals("000123", result.getTenantId());
        Assertions.assertEquals("华东租户", result.getTenantName());
    }
}
