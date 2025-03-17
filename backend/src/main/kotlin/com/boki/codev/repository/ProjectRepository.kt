package com.boki.codev.repository

import com.boki.codev.entity.project.Project
import com.boki.codev.entity.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ProjectRepository : JpaRepository<Project, Long> {

    @Modifying(clearAutomatically = true)
    @Query(
        value = "UPDATE projects SET project_status = 'STOP'" +
                "WHERE end_dt < :today AND project_status = 'ACTIVE'",
        nativeQuery = true
    )
    fun updateProjectsStatus(today: LocalDateTime): Int

    @Query(
        value = "SELECT p FROM Project p LEFT JOIN FETCH p.tags WHERE p.projectStatusWrapper.projectStatus != 'STOP' and p.owner = :owner",
    )
    fun findMyProjects(owner: User): List<Project>
}