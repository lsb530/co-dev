package com.boki.codev.dto

import com.boki.codev.entity.tag.Tag

data class TagResponse(
    val id: Long,
    val type: String,
    val name: String,
) {
    companion object {
        fun from(tag: Tag): TagResponse {
            return TagResponse(
                id = tag.id!!,
                type = tag.type.name,
                name = tag.name,
            )
        }
    }
}