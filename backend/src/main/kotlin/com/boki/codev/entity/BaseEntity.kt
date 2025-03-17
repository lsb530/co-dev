package com.boki.codev.entity

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(value = [AuditingEntityListener::class])
abstract class BaseEntity(
    @CreatedDate
    @Column(nullable = false, updatable = false)
    protected var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(nullable = true, updatable = true)
    protected var updatedAt: LocalDateTime? = null,

    @Column(nullable = true, updatable = true)
    protected var deletedAt: LocalDateTime? = null
) {
    fun softDelete() {
        this.deletedAt = LocalDateTime.now()
    }
}