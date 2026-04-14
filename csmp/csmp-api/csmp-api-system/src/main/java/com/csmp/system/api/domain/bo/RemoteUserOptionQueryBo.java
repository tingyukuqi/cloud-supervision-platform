package com.csmp.system.api.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 远程用户选项查询对象
 *
 * @author csmp
 */
@Data
public class RemoteUserOptionQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关键字，匹配账号、昵称、手机号
     */
    private String keyword;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 需要排除的用户ID列表
     */
    private List<Long> excludeUserIds;
}
