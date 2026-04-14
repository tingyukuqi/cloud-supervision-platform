package com.csmp.supply.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 供应商已绑定用户响应对象
 *
 * @author csmp
 */
@Data
public class SupplySupplierUserVo {

    private Long bindingId;
    private Long supplierId;
    private Long userId;
    private Long deptId;
    private String userName;
    private String nickName;
    private String phonenumber;
    private String email;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
