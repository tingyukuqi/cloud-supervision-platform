package com.csmp.supply.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 云平台响应对象
 *
 * @author csmp
 */
@Data
public class SupplyCloudPlatformVo {

    private Long platformId;
    private String platformCode;
    private String platformName;
    private String platformType;
    private String providerCode;
    private String resourcePoolCode;
    private String regionCode;
    private String accessUrl;
    private String apiVersion;
    private String description;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
