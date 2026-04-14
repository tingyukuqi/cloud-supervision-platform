package com.csmp.system.controller.system;

import com.csmp.common.core.domain.R;
import com.csmp.system.domain.vo.SysDictDataVo;
import com.csmp.system.service.ISysDictTypeService;
import com.csmp.system.service.ISysTenantService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SysTenantControllerTest {

    @Mock
    private ISysTenantService tenantService;

    @Mock
    private ISysDictTypeService dictTypeService;

    @Test
    public void typeOptionsShouldReadSysTenantTypeDict() {
        SysDictDataVo option = new SysDictDataVo();
        option.setDictLabel("平台运营");
        option.setDictValue("platform_operation");
        when(dictTypeService.selectDictDataByType("sys_tenant_type")).thenReturn(List.of(option));
        SysTenantController controller = new SysTenantController(tenantService, dictTypeService);

        R<List<SysDictDataVo>> result = controller.typeOptions();

        Assertions.assertEquals(R.SUCCESS, result.getCode());
        Assertions.assertEquals(1, result.getData().size());
        Assertions.assertEquals("platform_operation", result.getData().get(0).getDictValue());
        verify(dictTypeService).selectDictDataByType("sys_tenant_type");
    }
}
