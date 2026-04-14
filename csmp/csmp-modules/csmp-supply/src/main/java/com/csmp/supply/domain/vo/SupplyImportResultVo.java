package com.csmp.supply.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 导入结果
 *
 * @author csmp
 */
@Data
public class SupplyImportResultVo {

    private Integer totalCount;

    private Integer successCount;

    private Integer failureCount;

    private Integer updateCount;

    private List<String> failureMessages;
}
