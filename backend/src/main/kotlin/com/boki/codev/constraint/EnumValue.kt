package com.boki.codev.constraint

import com.boki.codev.validator.EnumValidator
import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented // To JavaDoc or Dokka
@Constraint(validatedBy = [EnumValidator::class])
annotation class EnumValue(
    val enumClass: KClass<out Enum<*>>,

    val message: String = "String에서 Enum으로의 변환과정에서 예외가 발생했습니다.",

    val groups: Array<KClass<*>> = [],

    val payload: Array<KClass<out Payload>> = [],

    val ignoreCase: Boolean = true
)