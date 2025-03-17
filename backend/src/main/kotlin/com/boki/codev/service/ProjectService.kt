package com.boki.codev.service

import com.boki.codev.dto.ProjectSimpleResponse
import com.boki.codev.repository.ProjectRepository
import com.boki.codev.repository.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProjectService(
    val userRepository: UserRepository,
    val projectRepository: ProjectRepository,
) {
    @Transactional(readOnly = true)
    fun getProjects(): List<ProjectSimpleResponse> {
        val authentication = SecurityContextHolder.getContext().authentication
        val user = (userRepository.findUserByEmail(authentication.name)
            ?: throw NoSuchElementException("User not found"))
        return projectRepository.findMyProjects(user).map(ProjectSimpleResponse::from)
    }
}