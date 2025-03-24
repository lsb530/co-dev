package com.boki.codev.security

import com.boki.codev.repository.ProjectRepository
import com.boki.codev.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component("projectPermission")
class ProjectPermissionEvaluator(
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository
) {
    fun isOwner(authentication: Authentication, projectId: Long): Boolean {
        val userEmail = authentication.name
        val user = userRepository.findUserByEmail(userEmail) ?: return false
        val project = projectRepository.findByIdOrNull(projectId) ?: return false
        return project.owner?.id == user.id
    }
}