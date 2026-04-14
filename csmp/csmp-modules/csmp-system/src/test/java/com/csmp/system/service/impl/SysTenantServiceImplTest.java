package com.csmp.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.csmp.common.core.constant.TenantConstants;
import com.csmp.common.core.utils.SpringUtils;
import com.csmp.system.domain.SysConfig;
import com.csmp.system.domain.SysDept;
import com.csmp.system.domain.SysDictData;
import com.csmp.system.domain.SysDictType;
import com.csmp.system.domain.SysRole;
import com.csmp.system.domain.SysRoleDept;
import com.csmp.system.domain.SysTenant;
import com.csmp.system.domain.SysTenantPackage;
import com.csmp.system.domain.SysUser;
import com.csmp.system.domain.bo.SysTenantBo;
import com.csmp.system.domain.vo.SysTenantVo;
import com.csmp.system.mapper.SysConfigMapper;
import com.csmp.system.mapper.SysDeptMapper;
import com.csmp.system.mapper.SysDictDataMapper;
import com.csmp.system.mapper.SysDictTypeMapper;
import com.csmp.system.mapper.SysRoleDeptMapper;
import com.csmp.system.mapper.SysRoleMapper;
import com.csmp.system.mapper.SysRoleMenuMapper;
import com.csmp.system.mapper.SysTenantMapper;
import com.csmp.system.mapper.SysTenantPackageMapper;
import com.csmp.system.mapper.SysUserMapper;
import com.csmp.system.mapper.SysUserRoleMapper;
import com.csmp.workflow.api.RemoteWorkflowService;
import io.github.linpeilie.Converter;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("dev")
class SysTenantServiceImplTest {

    @Mock
    private SysTenantMapper baseMapper;
    @Mock
    private SysTenantPackageMapper tenantPackageMapper;
    @Mock
    private SysUserMapper userMapper;
    @Mock
    private SysDeptMapper deptMapper;
    @Mock
    private SysRoleMapper roleMapper;
    @Mock
    private SysRoleMenuMapper roleMenuMapper;
    @Mock
    private SysRoleDeptMapper roleDeptMapper;
    @Mock
    private SysUserRoleMapper userRoleMapper;
    @Mock
    private SysDictTypeMapper dictTypeMapper;
    @Mock
    private SysDictDataMapper dictDataMapper;
    @Mock
    private SysConfigMapper configMapper;
    @Mock
    private RemoteWorkflowService remoteWorkflowService;

    @InjectMocks
    private SysTenantServiceImpl tenantService;

    @Test
    void normalizeVisibleTenantIdShouldAllowRequestedTenantForPlatformTenant() {
        SysTenantServiceImpl service = spy(tenantService);
        doReturn(TenantConstants.DEFAULT_TENANT_ID).when(service).loginTenantId();

        Assertions.assertNull(service.normalizeVisibleTenantId(null));
        Assertions.assertEquals("000123", service.normalizeVisibleTenantId("000123"));
    }

    @Test
    void normalizeVisibleTenantIdShouldForceCurrentTenantForNormalTenant() {
        SysTenantServiceImpl service = spy(tenantService);
        doReturn("000123").when(service).loginTenantId();

        Assertions.assertEquals("000123", service.normalizeVisibleTenantId(null));
        Assertions.assertEquals("000123", service.normalizeVisibleTenantId("000999"));
        Assertions.assertEquals("000123", service.normalizeVisibleTenantId("000123"));
    }

    @Test
    void queryByTenantIdShouldAllowPreLoginLookup() {
        SysTenantServiceImpl service = spy(tenantService);
        doReturn(null).when(service).loginTenantId();
        SysTenantVo tenant = new SysTenantVo();
        tenant.setTenantId("000123");
        when(baseMapper.selectVoOne(org.mockito.ArgumentMatchers.any())).thenReturn(tenant);

        SysTenantVo result = service.queryByTenantId("000123");

        Assertions.assertNotNull(result);
        Assertions.assertEquals("000123", result.getTenantId());
        verify(baseMapper).selectVoOne(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void queryListShouldFilterByTenantTypeWhenProvided() {
        SysTenantBo bo = new SysTenantBo();
        BeanUtil.setProperty(bo, "tenantType", "cloud_tenant");
        when(baseMapper.selectVoList(any())).thenReturn(List.of());

        tenantService.queryList(bo);

        ArgumentCaptor<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysTenant>> wrapperCaptor =
            ArgumentCaptor.forClass(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class);
        verify(baseMapper).selectVoList(wrapperCaptor.capture());
        Assertions.assertTrue(
            wrapperCaptor.getValue().getSqlSegment().contains("tenant_type"),
            "租户类型过滤条件未写入查询 SQL"
        );
    }

    @Test
    void insertByBoShouldNotCreateDefaultDept() {
        SysTenantBo bo = new SysTenantBo();
        bo.setCompanyName("测试租户");
        bo.setUsername("tenant_admin");
        bo.setPassword("123456");
        bo.setPackageId(1L);
        BeanUtil.setProperty(bo, "tenantType", "cloud_tenant");

        SysTenantPackage tenantPackage = new SysTenantPackage();
        tenantPackage.setPackageId(1L);
        tenantPackage.setMenuIds("1,2");

        GenericApplicationContext applicationContext = new GenericApplicationContext();
        Converter converter = org.mockito.Mockito.mock(Converter.class);
        applicationContext.registerBean(Converter.class, () -> converter);
        applicationContext.refresh();
        new SpringUtils().setApplicationContext(applicationContext);
        when(converter.convert(any(SysTenantBo.class), org.mockito.ArgumentMatchers.eq(SysTenant.class)))
            .thenAnswer(invocation -> {
                SysTenantBo source = invocation.getArgument(0);
                SysTenant tenant = new SysTenant();
                BeanUtil.setProperty(tenant, "tenantType", BeanUtil.getProperty(source, "tenantType"));
                return tenant;
            });
        initTableInfo(SysTenant.class);
        initTableInfo(SysDictType.class);
        initTableInfo(SysDictData.class);
        initTableInfo(SysConfig.class);
        ReflectionTestUtils.setField(tenantService, "remoteWorkflowService", remoteWorkflowService);

        when(baseMapper.selectObjs(any(), org.mockito.ArgumentMatchers.<Function<Object, String>>any()))
            .thenReturn(Collections.emptyList());
        when(baseMapper.insert(any(SysTenant.class))).thenReturn(1);
        when(tenantPackageMapper.selectById(1L)).thenReturn(tenantPackage);
        when(roleMapper.insert(any(SysRole.class))).thenAnswer(invocation -> {
            SysRole role = invocation.getArgument(0);
            role.setRoleId(100L);
            return 1;
        });
        when(dictTypeMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(dictDataMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(configMapper.selectList(any())).thenReturn(Collections.emptyList());

        Boolean result = tenantService.insertByBo(bo);

        Assertions.assertTrue(result);
        verify(deptMapper, never()).insert(any(SysDept.class));
        verify(deptMapper, never()).updateById(any(SysDept.class));
        verify(roleDeptMapper, never()).insert(any(SysRoleDept.class));

        ArgumentCaptor<SysTenant> tenantCaptor = ArgumentCaptor.forClass(SysTenant.class);
        verify(baseMapper).insert(tenantCaptor.capture());
        Assertions.assertEquals("cloud_tenant", BeanUtil.getProperty(tenantCaptor.getValue(), "tenantType"));

        ArgumentCaptor<SysUser> userCaptor = ArgumentCaptor.forClass(SysUser.class);
        verify(userMapper).insert(userCaptor.capture());
        Assertions.assertNull(userCaptor.getValue().getDeptId());
        verify(remoteWorkflowService).syncDef(any());
    }

    private void initTableInfo(Class<?> entityClass) {
        if (TableInfoHelper.getTableInfo(entityClass) != null) {
            return;
        }
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), entityClass);
    }
}
