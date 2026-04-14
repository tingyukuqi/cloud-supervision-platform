package com.csmp.supply.service.impl;

import com.csmp.common.core.constant.TenantConstants;
import com.csmp.common.core.exception.ServiceException;
import com.csmp.common.core.utils.StringUtils;
import com.csmp.common.json.utils.JsonUtils;
import com.csmp.common.satoken.utils.LoginHelper;
import com.csmp.common.tenant.helper.TenantHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 供应链服务基类
 *
 * @author csmp
 */
public abstract class AbstractSupplyService {

    private static final String[] NO_AUTH_TYPES = {"none", "no_auth", "anonymous"};

    protected String currentTenantId() {
        String tenantId = TenantHelper.getTenantId();
        if (StringUtils.isBlank(tenantId)) {
            tenantId = LoginHelper.getTenantId();
        }
        if (StringUtils.isBlank(tenantId)) {
            throw new ServiceException("当前租户上下文不存在");
        }
        return tenantId;
    }

    protected boolean hasGlobalTenantDataAccess() {
        return StringUtils.equals(currentTenantId(), TenantConstants.DEFAULT_TENANT_ID);
    }

    protected String queryTenantScope() {
        return hasGlobalTenantDataAccess() ? null : currentTenantId();
    }

    protected String resolveTargetTenantId(String tenantId) {
        return StringUtils.defaultIfBlank(tenantId, currentTenantId());
    }

    protected Long currentUserId() {
        return LoginHelper.getUserId();
    }

    protected Object parseJsonObject(String text) {
        return JsonUtils.parseMap(text);
    }

    protected void validateHttpUrl(String value, String fieldName) {
        if (StringUtils.isBlank(value) || !StringUtils.ishttp(value)) {
            throw new ServiceException("{}格式不正确，必须以 http:// 或 https:// 开头", fieldName);
        }
    }

    protected void validateOptionalHttpUrl(String value, String fieldName) {
        if (StringUtils.isNotBlank(value) && !StringUtils.ishttp(value)) {
            throw new ServiceException("{}格式不正确，必须以 http:// 或 https:// 开头", fieldName);
        }
    }

    protected void validateEndpointPath(String value, String fieldName) {
        if (StringUtils.isBlank(value) || !StringUtils.startsWith(value, StringUtils.SLASH)) {
            throw new ServiceException("{}不能为空且必须以 / 开头", fieldName);
        }
    }

    protected void validateOptionalEndpointPath(String value, String fieldName) {
        if (StringUtils.isNotBlank(value) && !StringUtils.startsWith(value, StringUtils.SLASH)) {
            throw new ServiceException("{}格式不正确，必须以 / 开头", fieldName);
        }
    }

    protected boolean requiresAuthPayload(String authType) {
        return StringUtils.isNotBlank(authType) && !StringUtils.inStringIgnoreCase(authType, NO_AUTH_TYPES);
    }

    protected void validateCreditCode(String creditCode) {
        if (StringUtils.isNotBlank(creditCode) && !creditCode.matches("^[0-9A-Z]{18}$")) {
            throw new ServiceException("统一社会信用代码格式不正确");
        }
    }

    protected Date parseDateTime(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(text);
        } catch (ParseException e) {
            throw new ServiceException("时间格式不正确: {}", text);
        }
    }
}
