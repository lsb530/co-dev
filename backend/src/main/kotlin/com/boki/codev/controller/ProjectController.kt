package com.boki.codev.controller

import com.boki.codev.dto.OwnerChangeRequest
import com.boki.codev.dto.ProjectCreateRequest
import com.boki.codev.dto.ProjectUpdateRequest
import com.boki.codev.service.ProjectService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.net.URI

@RequestMapping("/api/v1/projects")
@RestController
class ProjectController(
    private val projectService: ProjectService,
) {
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    fun getProjects(): ResponseEntity<Any> {
        return ResponseEntity.ok().body(projectService.getProjects())
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @projectPermission.isOwner(authentication, #projectId)")
    @GetMapping("/{projectId}")
    fun getProject(
        @PathVariable projectId: Long
    ): ResponseEntity<Any> {
        return ResponseEntity.ok().body(projectService.getProject(projectId))
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    fun createProject(
        @Valid @RequestBody projectCreateRequest: ProjectCreateRequest
    ): ResponseEntity<Any> {
        val createProject = projectService.createProject(projectCreateRequest)
        val uri = URI.create("/api/v1/projects/${createProject.id}")
        return ResponseEntity.created(uri).body(createProject)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @projectPermission.isOwner(authentication, #projectId)")
    @PatchMapping("/{projectId}")
    fun updateProject(
        @PathVariable projectId: Long,
        @Valid @RequestBody projectUpdateRequest: ProjectUpdateRequest
    ): ResponseEntity<Any> {
        return ResponseEntity.ok().body(projectService.updateProject(projectId, projectUpdateRequest))
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/{projectId}/owner")
    fun updateProjectOwner(
        @PathVariable projectId: Long,
        @Valid @RequestBody ownerChangeRequest: OwnerChangeRequest
    ): ResponseEntity<Any> {
        return ResponseEntity.ok().body(projectService.updateProjectOwner(projectId, ownerChangeRequest.ownerId))
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{projectId}")
    fun deleteProject(
        @PathVariable projectId: Long
    ) : ResponseEntity<Any> {
        projectService.deleteProject(projectId)
        return ResponseEntity.noContent().build()
    }
}
