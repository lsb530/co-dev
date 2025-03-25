package com.boki.codev.service

import com.boki.codev.dto.ProjectCreateRequest
import com.boki.codev.dto.ProjectSimpleResponse
import com.boki.codev.dto.ProjectUpdateRequest
import com.boki.codev.entity.project.Project
import com.boki.codev.entity.project.ProjectStatus
import com.boki.codev.entity.tag.TagType
import com.boki.codev.exception.NotFoundException
import com.boki.codev.repository.ProjectRepository
import com.boki.codev.repository.TagRepository
import com.boki.codev.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
class ProjectService(
    val userRepository: UserRepository,
    val projectRepository: ProjectRepository,
    val tagRepository: TagRepository,
) {
    @Transactional(readOnly = true)
    fun getProjects(): List<ProjectSimpleResponse> {
        logger.debug { "ProjectService.getProjects() call" }
        val authentication = SecurityContextHolder.getContext().authentication
        return if ("admin@co-dev.com" == authentication.name) {
            projectRepository.findAll().map { ProjectSimpleResponse.from(it) }
        } else {
            val user = (userRepository.findUserByEmail(authentication.name)
                ?: throw NotFoundException("User not found"))
            projectRepository.findMyProjects(user).map(ProjectSimpleResponse::from)
        }
    }

    @Transactional(readOnly = true)
    fun getProject(projectId: Long): ProjectSimpleResponse {
        logger.debug { "ProjectService.getProject() call" }
        val project = getProjectOrThrow(projectId)
        return ProjectSimpleResponse.from(project)
    }

    @Transactional
    fun createProject(projectCreateRequest: ProjectCreateRequest): ProjectSimpleResponse {
        logger.debug { "ProjectService.createProject() call" }
        val owner = userRepository.findByIdOrNull(projectCreateRequest.ownerId)
            ?: throw NotFoundException("User not found: ${projectCreateRequest.ownerId}")
        check(owner.isManager) {
            "Only manager position can manage projects(#ownerId: ${owner.id})"
        }

        val tagSet = projectCreateRequest.tags?.map { tagRepository.findById(it).orElse(null) }
            ?.filter { it.type == TagType.PROJECT }
            ?.map { it.name }?.toMutableSet() ?: mutableSetOf()

        val newProject = projectRepository.save(projectCreateRequest.toEntity(owner, tagSet))
        return ProjectSimpleResponse.from(newProject)
    }

    @Transactional
    fun updateProject(
        projectId: Long,
        projectUpdateRequest: ProjectUpdateRequest
    ): ProjectSimpleResponse {
        logger.debug { "ProjectService.updateProject() call" }
        val project = getProjectOrThrow(projectId)

        val (
            description,
            status,
            endDt,
            tags
        ) = projectUpdateRequest

        if (status != null) {
            val projectStatus = ProjectStatus.valueOf(status)
            project.projectStatusWrapper.updateStatus(projectStatus)
        }

        val tagSet = tags?.map { tagRepository.findById(it).orElse(null) }
            ?.filter { it.type == TagType.PROJECT }
            ?.map { it.name }?.toMutableSet() ?: mutableSetOf()

        project.description = description ?: project.description
        project.endDt = endDt ?: project.endDt
        project.tags = tagSet

        return ProjectSimpleResponse.from(project)
    }

    @Transactional
    fun updateProjectOwner(
        projectId: Long,
        ownerId: Long?,
    ): ProjectSimpleResponse {
        logger.debug { "ProjectService.updateProjectOwner() call" }
        val project = getProjectOrThrow(projectId)

        val updateOwner = if (ownerId != null) {
            val user = userRepository.findByIdOrNull(ownerId)
                ?: throw NotFoundException("Owner not found: $ownerId")
            check(user.isManager) {
                "Only manager position can manage projects(#ownerId: ${ownerId})"
            }
            user
        } else {
            null
        }
        project.owner = updateOwner

        return ProjectSimpleResponse.from(project)
    }

    @Transactional
    fun deleteProject(projectId: Long) {
        logger.debug { "ProjectService.deleteProject() call" }
        val project = getProjectOrThrow(projectId)
        project.softDelete()
    }

    private fun getProjectOrThrow(projectId: Long): Project {
        return projectRepository.findByIdOrNull(projectId)
            ?: throw NotFoundException("Project not found: $projectId")
    }
}