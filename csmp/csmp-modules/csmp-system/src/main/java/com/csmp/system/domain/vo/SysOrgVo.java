package com.csmp.system.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import com.csmp.common.excel.annotation.ExcelDictFormat;
import com.csmp.common.excel.convert.ExcelDictConvert;
import com.csmp.system.domain.SysDept;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 组织机构视图对象 sys_dept（parent_id = 0 的根部门）
 *
 * @author csmp
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SysDept.class)
public class SysOrgVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 组织ID（即部门ID）
     */
    @ExcelProperty(value = "组织ID")
    private Long orgId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 租户名称
     */
    @ExcelProperty(value = "租户名称")
    private String tenantName;

    /**
     * 组织名称（即部门名称）
     */
    @ExcelProperty(value = "组织名称")
    private String orgName;

    /**
     * 组织类型（复用 dept_category 字段）
     */
    @ExcelProperty(value = "组织类型")
    private String deptCategory;

    /**
     * 显示顺序
     */
    @ExcelProperty(value = "显示顺序")
    private Integer orderNum;

    /**
     * 负责人ID
     */
    private Long leader;

    /**
     * 负责人姓名
     */
    @ExcelProperty(value = "负责人")
    private String leaderName;

    /**
     * 联系电话
     */
    @ExcelProperty(value = "联系电话")
    private String phone;

    /**
     * 邮箱
     */
    @ExcelProperty(value = "邮箱")
    private String email;

    /**
     * 状态（0正常 1停用）
     */
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_normal_disable")
    private String status;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

}
