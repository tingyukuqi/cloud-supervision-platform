package com.csmp.system.controller.system;

import cn.hutool.core.lang.tree.Tree;
import com.csmp.common.core.domain.R;
import com.csmp.system.domain.bo.SysOrgBo;
import com.csmp.system.domain.vo.SysOrgVo;
import com.csmp.system.service.ISysDeptService;
import com.csmp.system.service.ISysOrgService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("dev")
class SysOrgControllerTest {

    @Mock
    private ISysOrgService orgService;
    @Mock
    private ISysDeptService deptService;

    @InjectMocks
    private SysOrgController controller;

    @Test
    void getInfoShouldCheckDeptDataScope() {
        controller.getInfo(9L);

        verify(deptService).checkDeptDataScope(9L);
        verify(orgService).selectOrgById(9L);
    }

    @Test
    void deptTreeShouldCheckDeptDataScopeAndUseOrgScopedTree() {
        controller.deptTree(9L);

        verify(deptService).checkDeptDataScope(9L);
        verify(deptService).selectDeptTreeByOrgId(9L);
        verify(deptService, never()).selectDeptTreeList(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void removeShouldCheckDeptDataScopeBeforeDelete() {
        controller.remove(new Long[]{9L, 10L});

        verify(deptService).checkDeptDataScope(9L);
        verify(deptService).checkDeptDataScope(10L);
        verify(orgService).deleteOrgById(9L);
        verify(orgService).deleteOrgById(10L);
    }

    @Test
    void listShouldReturnGuidanceMessageWhenEmpty() {
        when(orgService.selectOrgList(org.mockito.ArgumentMatchers.any(SysOrgBo.class))).thenReturn(List.of());

        R<List<SysOrgVo>> result = controller.list(new SysOrgBo());

        org.junit.jupiter.api.Assertions.assertEquals("当前租户暂无组织机构，请先新增组织机构", result.getMsg());
        org.junit.jupiter.api.Assertions.assertNotNull(result.getData());
        org.junit.jupiter.api.Assertions.assertTrue(result.getData().isEmpty());
    }

    @Test
    void treeShouldReturnGuidanceMessageWhenEmpty() {
        when(orgService.selectOrgTreeSelect()).thenReturn(List.of());

        R<List<Tree<Long>>> result = controller.tree();

        org.junit.jupiter.api.Assertions.assertEquals("当前租户暂无组织机构，请先新增组织机构", result.getMsg());
        org.junit.jupiter.api.Assertions.assertNotNull(result.getData());
        org.junit.jupiter.api.Assertions.assertTrue(result.getData().isEmpty());
    }
}
