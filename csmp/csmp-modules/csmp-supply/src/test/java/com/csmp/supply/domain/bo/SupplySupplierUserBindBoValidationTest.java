package com.csmp.supply.domain.bo;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

@Tag("dev")
public class SupplySupplierUserBindBoValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }

    @Test
    void shouldAllowEmptyUserIdsForClearingBindings() {
        SupplySupplierUserBindBo bo = new SupplySupplierUserBindBo();
        bo.setUserIds(List.of());

        Set<ConstraintViolation<SupplySupplierUserBindBo>> violations = validator.validate(bo);

        Assertions.assertTrue(violations.isEmpty());
    }

    @Test
    void shouldRejectNullUserIds() {
        SupplySupplierUserBindBo bo = new SupplySupplierUserBindBo();

        Set<ConstraintViolation<SupplySupplierUserBindBo>> violations = validator.validate(bo);

        Assertions.assertEquals(1, violations.size());
        Assertions.assertEquals("用户ID不能为空", violations.iterator().next().getMessage());
    }
}
