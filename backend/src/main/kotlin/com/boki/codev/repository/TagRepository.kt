package com.boki.codev.repository

import com.boki.codev.entity.tag.Tag
import org.springframework.data.jpa.repository.JpaRepository

interface TagRepository: JpaRepository<Tag, Long> {
    fun findByNameIgnoreCase(name: String): Tag?
}