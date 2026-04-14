package com.csmp.system.domain.bo;

import com.csmp.common.core.validate.AddGroup;
import com.csmp.common.core.validate.EditGroup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.csmp.common.mybatis.core.domain.BaseEntity;
import com.csmp.system.domain.SysDept;
import io.github.linpeilie.annotations.AutoMapper;

/**
 * 组织机构业务对象 sys_dept（parent_id = 0 的根部门）
 *
 * @author csmp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysDept.class, reverseConvertGenerate = false)
public class SysOrgBo extends BaseEntity {

    /**
     * 组织ID（即部门ID）
     */
    @NotNull(message = "组织ID不能为空", groups = EditGroup.class)
    private Long orgId;

    /**
     * 租户ID
     */
    @NotBlank(message = "租户ID不能为空", groups = AddGroup.class)
    private String tenantId;

    /**
     * 组织名称
     */
    @NotBlank(message = "组织名称不能为空")
    @Size(min = 0, max = 30, message = "组织名称长度不能超过{max}个字符")
    private String orgName;

    /**
     * 组织类型（复用 dept_category 字段）
     */
    @Size(min = 0, max = 100, message = "组织类型长度不能超过{max}个字符")
    private String deptCategory;

    /**
     * 显示顺序
     */
    @NotNull(message = "显示顺序不能为空")
    private Integer orderNum;

    /**
     * 负责人ID
     */
    private Long leader;

    /**
     * 联系电话
     */
    @Size(min = 0, max = 11, message = "联系电话长度不能超过{max}个字符")
    private String phone;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(min = 0, max = 50, message = "邮箱长度不能超过{max}个字符")
    private String email;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

}
