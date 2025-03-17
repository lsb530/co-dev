package com.boki.codev.dto

import com.boki.codev.constraint.EnumValue
import com.boki.codev.entity.project.Project
import com.boki.codev.entity.project.ProjectStatus
import com.boki.codev.entity.project.ProjectStatusWrapper
import com.boki.codev.entity.user.User
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class ProjectCreateRequest(
    @field:NotBlank(message = "프로젝트 이름은 비어있을 수 없습니다.")
    val name: String?,

    @field:NotBlank(message = "설명은 비어있을 수 없습니다.")
    val description: String?,

    @field:NotBlank(message = "프로젝트 상태는 비어있을 수 없습니다.")
    @field:EnumValue(enumClass = ProjectStatus::class, message = "프로젝트 상태가 잘못 입력되었습니다.")
    private val status: String?,

    @field:FutureOrPresent(message = "프로젝트 시작일은 과거일 수 없습니다.")
    val startDt: LocalDateTime? = LocalDateTime.now(),

    @field:Future(message = "프로젝트 종료일은 과거일 수 없습니다.")
    val endDt: LocalDateTime? = null,

    val owner: User?,

    private val tags: List<Long>? = null ?: emptyList(),
) {
    val projectStatus: ProjectStatus?
        get() = status?.let { ProjectStatus.valueOf(it) }

    companion object {
        fun toEntity(req: ProjectCreateRequest): Project {
            return Project(
                name = requireNotNull(req.name),
                description = requireNotNull(req.description),
                projectStatusWrapper = ProjectStatusWrapper(requireNotNull(req.projectStatus)),
                startDt = req.startDt,
                endDt = req.endDt,
                owner = req.owner,
            )
        }
    }
}

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