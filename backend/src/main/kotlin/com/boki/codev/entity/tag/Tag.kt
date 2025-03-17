package com.boki.codev.entity.tag

import com.boki.codev.entity.BaseEntity
import jakarta.persistence.*

@Table(name = "tags")
@Entity
class Tag(
    @Column(nullable = false, unique = true)
    val name: String,

    @Enumerated(EnumType.STRING)
    val type: TagType,

    val isCustom: Boolean = false,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) : BaseEntity() {
    constructor(name: String, type: TagType) : this(name = name, type = type, isCustom = true)
}