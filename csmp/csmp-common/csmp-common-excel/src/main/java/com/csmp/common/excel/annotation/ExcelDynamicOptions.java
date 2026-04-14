package com.csmp.common.excel.annotation;

import com.csmp.common.excel.core.ExcelOptionsProvider;

import java.lang.annotation.*;

/**
 * Excel动态下拉选项注解
 *
 * @author Angus
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelDynamicOptions {

    /**
     * 提供者类全限定名
     * 实现com.csmp.common.excel.service.ExcelOptionsProvider实现类接口
     */
    Class<? extends ExcelOptionsProvider> providerClass();
}
