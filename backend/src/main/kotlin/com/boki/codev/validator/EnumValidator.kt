package com.boki.codev.validator

import com.boki.codev.constraint.EnumValue
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class EnumValidator : ConstraintValidator<EnumValue, String> {
    private lateinit var enumConstants: Array<out Enum<*>>
    private var ignoreCase: Boolean = false

    override fun initialize(constraintAnnotation: EnumValue) {
        enumConstants = constraintAnnotation.enumClass.java.enumConstants
        ignoreCase = constraintAnnotation.ignoreCase
    }

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) {
            return true
        }
        return enumConstants.any { enumConstant ->
            if (ignoreCase) {
                enumConstant.name.equals(value, ignoreCase = true)
            } else {
                enumConstant.name == value
            }
        }
    }
}
