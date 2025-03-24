package com.boki.codev.provision

import com.boki.codev.entity.project.Project
import com.boki.codev.entity.project.ProjectStatus
import com.boki.codev.entity.project.ProjectStatusWrapper
import com.boki.codev.entity.tag.Tag
import com.boki.codev.entity.tag.TagType
import com.boki.codev.entity.user.Role
import com.boki.codev.entity.user.User
import com.boki.codev.repository.ProjectRepository
import com.boki.codev.repository.TagRepository
import com.boki.codev.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Profile("!prod")
@Component
class DataInitializer(
    private val userRepository: UserRepository,
    private val tagRepository: TagRepository,
    private val projectRepository: ProjectRepository,
) : ApplicationListener<ApplicationReadyEvent> {

    @Transactional
    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        logger.info { "Application started running" }
        logger.info { "<< Database initialize job started" }
        initUsers(userRepository)
        initTags(tagRepository)
        initProjects(projectRepository)
        logger.info { ">> Database initialize job end" }
        // deleteUser()
        // getUsers()
    }

    fun isEmpty(repository: JpaRepository<*, *>): Boolean {
        return repository.count() == 0L
    }

    fun initUsers(userRepository: UserRepository) {
        if (isEmpty(userRepository)) {
            val admin = User(username = "admin", email = "admin@co-dev.com", password = "admin", role = Role.ADMIN)
            val manager1 = User(username = "manager1", email = "manager1@co-dev.com", password = "manager", role = Role.MANAGER)
            val manager2 = User(username = "manager2", email = "manager2@co-dev.com", password = "manager", role = Role.MANAGER)
            val worker = User(username = "worker", email = "worker@co-dev.com", password = "worker", role = Role.WORKER)

            userRepository.saveAll(
                listOf(admin, manager1, manager2, worker)
            )
        }
    }

    fun deleteUser() {
        val user = userRepository.findById(1L).orElseThrow()
        user.softDelete()
    }

    fun getUsers() {
        val users = userRepository.findAll()
        for (user in users) {
            println(user)
        }
    }

    fun initTags(tagRepository: TagRepository) {
        if (isEmpty(tagRepository)) {
            val projectTags = listOf(
                Tag(name = "Backend", type = TagType.PROJECT, isCustom = false),
                Tag(name = "Frontend", type = TagType.PROJECT, isCustom = false),
                Tag(name = "Mobile", type = TagType.PROJECT, isCustom = false),
                Tag(name = "Devops", type = TagType.PROJECT, isCustom = false),
            )

            val taskTags = listOf(
                Tag(name = "Urgent", type = TagType.TASK, isCustom = false),
                Tag(name = "High", type = TagType.TASK, isCustom = false),
                Tag(name = "Normal", type = TagType.TASK, isCustom = false),
                Tag(name = "Low", type = TagType.TASK, isCustom = false),
            )

            tagRepository.saveAll(projectTags)
            tagRepository.saveAll(taskTags)
        }
    }

    fun initProjects(projectRepository: ProjectRepository) {
        if (isEmpty(projectRepository)) {
            val tagNames = listOfNotNull(
                tagRepository.findByNameIgnoreCase("Backend")?.name,
                tagRepository.findByNameIgnoreCase("Frontend")?.name,
                tagRepository.findByNameIgnoreCase("Devops")?.name
            ).toMutableSet()

            val projects = listOf(
                Project(
                    name = "project1",
                    description = "설명1",
                    projectStatusWrapper = ProjectStatusWrapper(projectStatus = ProjectStatus.ACTIVE),
                    startDt = LocalDateTime.now().minusDays(6),
                    endDt = LocalDateTime.now().minusDays(2),
                    owner = userRepository.findById(1L).orElse(null),
                    tags = tagNames
                ),
                Project(
                    name = "project2",
                    description = "설명2",
                    projectStatusWrapper = ProjectStatusWrapper(projectStatus = ProjectStatus.BACKLOG),
                    startDt = LocalDateTime.now().minusDays(10),
                    endDt = LocalDateTime.now().minusDays(1),
                    owner = userRepository.findById(1L).orElse(null),
                ),
                Project(
                    name = "project3",
                    description = "설명3",
                    projectStatusWrapper = ProjectStatusWrapper(projectStatus = ProjectStatus.COMPLETED),
                    startDt = LocalDateTime.now().minusDays(10),
                    endDt = LocalDateTime.now().minusDays(1),
                    owner = userRepository.findById(1L).orElse(null),
                ),
                Project(
                    name = "project4",
                    description = "설명4",
                    projectStatusWrapper = ProjectStatusWrapper(projectStatus = ProjectStatus.ACTIVE),
                    startDt = LocalDateTime.now().minusDays(10),
                    endDt = LocalDateTime.now().minusDays(1),
                    owner = userRepository.findById(2L).orElse(null),
                ),
                Project(
                    name = "project5",
                    description = "설명5",
                    projectStatusWrapper = ProjectStatusWrapper(projectStatus = ProjectStatus.ACTIVE),
                    startDt = LocalDateTime.now().minusDays(10),
                    endDt = LocalDateTime.now().minusDays(1),
                    owner = userRepository.findById(2L).orElse(null),
                ),
            )

            projectRepository.saveAll(projects)
        }
    }
}