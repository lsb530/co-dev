package com.boki.codev.dto

import com.boki.codev.constraint.EnumValue
import com.boki.codev.entity.project.Project
import com.boki.codev.entity.project.ProjectStatus
import com.boki.codev.entity.project.ProjectStatusWrapper
import com.boki.codev.entity.user.User
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import java.time.LocalDateTime

data class ProjectCreateRequest(
    @field:NotBlank(message = "프로젝트 이름은 비어있을 수 없습니다.")
    val name: String?,

    @field:NotBlank(message = "설명은 비어있을 수 없습니다.")
    val description: String?,

    @field:NotBlank(message = "프로젝트 상태는 비어있을 수 없습니다.")
    @field:EnumValue(enumClass = ProjectStatus::class, message = "프로젝트 상태가 잘못 입력되었습니다.")
    val status: String?,

    @field:FutureOrPresent(message = "프로젝트 시작일은 과거일 수 없습니다.")
    val startDt: LocalDateTime? = LocalDateTime.now(),

    @field:Future(message = "프로젝트 종료일은 과거일 수 없습니다.")
    val endDt: LocalDateTime? = null,

    @field:NotNull(message = "프로젝트 초기 담당자는 비어있을 수 없습니다.")
    val ownerId: Long,

    val tags: List<Long>? = emptyList(),
) {
    val projectStatus: ProjectStatus?
        get() = status?.let { ProjectStatus.valueOf(it) }

    fun toEntity(owner: User, tags: MutableSet<String>): Project {
        return Project(
            name = requireNotNull(name),
            description = requireNotNull(description),
            projectStatusWrapper = ProjectStatusWrapper(requireNotNull(projectStatus)),
            startDt = startDt,
            endDt = endDt,
            owner = owner,
            tags = tags
        )
    }
}

data class ProjectUpdateRequest(
    val description: String?,

    @field:EnumValue(enumClass = ProjectStatus::class, message = "프로젝트 상태(status)가 잘못 입력되었습니다.")
    val status: String?,

    @field:Future(message = "프로젝트 종료일은 과거일 수 없습니다.")
    val endDt: LocalDateTime? = null,

    val tags: List<Long>? = emptyList(),
)

data class OwnerChangeRequest(
    @field:PositiveOrZero(message = "ownerId는 null 또는 양수여야 합니다.")
    val ownerId: Long?
)

data class ProjectSimpleResponse(
    val id: Long,
    val name: String,
    val description: String,
    val status: String,
    val owner: String?,
    val startDt: LocalDateTime?,
    val endDt: LocalDateTime?,
    // val workers: MutableList<Worker>?,
    // val tasks: MutableList<Task>,
    val tags: List<String>? = emptyList(),
) {
    companion object {
        fun from(project: Project): ProjectSimpleResponse {
            return ProjectSimpleResponse(
                id = project.id!!,
                name = project.name,
                description = project.description,
                status = project.projectStatusWrapper.projectStatus.name,
                startDt = project.startDt,
                endDt = project.endDt,
                owner = project.owner?.email,
                tags = project.tags.toList()
            )
        }
    }
}