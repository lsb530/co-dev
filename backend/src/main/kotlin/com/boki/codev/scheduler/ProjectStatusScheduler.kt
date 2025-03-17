package com.boki.codev.scheduler

import com.boki.codev.repository.ProjectRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Component
class ProjectStatusScheduler(
    private val projectRepository: ProjectRepository,
) {
    @Async("taskExecutor")
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    fun updateProjectsStatus() {
        logger.warn { "Update projects start" }

        val today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0)
        val updatedCount = projectRepository.updateProjectsStatus(today)

        logger.warn { "Updated $updatedCount projects" }
    }
}