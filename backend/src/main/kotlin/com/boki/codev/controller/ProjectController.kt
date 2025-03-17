package com.boki.codev.controller

import com.boki.codev.service.ProjectService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v1/projects")
@RestController
class ProjectController(
    private val projectService: ProjectService,
) {
    @PreAuthorize("isAuthenticated()")
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getProjects(): ResponseEntity<Any> {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(projectService.getProjects())
    }
}
