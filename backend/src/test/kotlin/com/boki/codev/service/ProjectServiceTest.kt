package com.boki.codev.service

import com.boki.codev.dto.ProjectCreateRequest
import com.boki.codev.dto.ProjectUpdateRequest
import com.boki.codev.entity.project.Project
import com.boki.codev.entity.project.ProjectStatus
import com.boki.codev.entity.project.ProjectStatusWrapper
import com.boki.codev.entity.tag.Tag
import com.boki.codev.entity.task.Task
import com.boki.codev.entity.worker.Worker
import com.boki.codev.exception.NotFoundException
import com.boki.codev.fixture.*
import com.boki.codev.repository.ProjectRepository
import com.boki.codev.repository.TagRepository
import com.boki.codev.repository.UserRepository
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.context.TestSecurityContextHolder
import java.time.LocalDateTime
import java.util.Optional

class ProjectServiceTest : BehaviorSpec({
    val userRepository = mockk<UserRepository>()
    val projectRepository = mockk<ProjectRepository>()
    val tagRepository = mockk<TagRepository>()
    val projectService = ProjectService(userRepository, projectRepository, tagRepository)

    val tag = mockk<Tag>(relaxed = true)

    afterTest {
        clearAllMocks()
        TestSecurityContextHolder.clearContext()
    }

    Given("프로젝트 목록 조회 요청 시") {
        val adminEmail = "admin@co-dev.com"
        val managerEmail = "manager1@co-dev.com"

        val adminAuth = TestingAuthenticationToken(adminEmail, "admin", "ROLE_ADMIN")
        val managerAuth = TestingAuthenticationToken(managerEmail, "manager", "ROLE_MANAGER")
        val anonymousAuth = AnonymousAuthenticationToken(
            "anonymous",
            "anonymousUser",
            listOf(SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        )

        val adminProjects = sut.giveMeKotlinBuilder<Project>()
            .setNotNull("id")
            .set("owner", adminUser)
            .set("workers", mutableListOf<Worker>())
            .set("tasks", mutableListOf<Task>())
            .sampleList(1)

        val managerProjects = sut.giveMeKotlinBuilder<Project>()
            .setNotNull("id")
            .set("owner", managerUser1)
            .set("workers", mutableListOf<Worker>())
            .set("tasks", mutableListOf<Task>())
            .sampleList(2)

        val allProjects = adminProjects + managerProjects

        When("로그인하지 않은 경우") {
            TestSecurityContextHolder.getContext().authentication = anonymousAuth

            every { userRepository.findUserByEmail(any()) } returns null

            Then("권한 없음 예외가 발생한다") {
                shouldThrow<NoSuchElementException> {
                    projectService.getProjects()
                }

                verify(exactly = 1) { userRepository.findUserByEmail(any()) }
                verify(exactly = 0) { projectRepository.findMyProjects(any()) }
            }
        }

        When("관리자(ADMIN) 권한으로 로그인한 경우") {
            TestSecurityContextHolder.getContext().authentication = adminAuth

            every { userRepository.findUserByEmail(adminEmail) } returns adminUser
            every { projectRepository.findMyProjects(adminUser) } returns adminProjects
            every { projectRepository.findAll() } returns allProjects

            Then("모든 프로젝트 목록이 조회된다") {
                val result = projectService.getProjects()

                result.size shouldBe 3
                result[0].owner shouldBe adminEmail

                verify(exactly = 1) { projectRepository.findAll() }
                verify(exactly = 0) { userRepository.findUserByEmail(adminEmail) }
                verify(exactly = 0) { projectRepository.findMyProjects(adminUser) }
            }
        }

        When("매니저(MANAGER) 권한으로 로그인한 경우") {
            TestSecurityContextHolder.getContext().authentication = managerAuth

            every { userRepository.findUserByEmail(managerEmail) } returns managerUser1
            every { projectRepository.findMyProjects(managerUser1) } returns managerProjects

            Then("본인이 소유한 프로젝트만 조회된다") {
                val result = projectService.getProjects()

                result.size shouldBe 2
                result[0].owner shouldBe managerEmail

                verify(exactly = 0) { projectRepository.findAll() }
                verify(exactly = 1) { userRepository.findUserByEmail(managerEmail) }
                verify(exactly = 1) { projectRepository.findMyProjects(managerUser1) }
            }
        }
    }

    Given("프로젝트 단건 조회 요청 시") {
        val projectName = "기존의 프로젝트"
        val projectDescription = "description"
        val projectId = 1L

        val existingProject = sut.giveMeKotlinBuilder<Project>()
            .set("id", projectId)
            .set("name", projectName)
            .set("description", projectDescription)
            .set("projectStatusWrapper", ProjectStatusWrapper(ProjectStatus.ACTIVE))
            .set("owner", managerUser1)
            .set("workers", mutableListOf<Worker>())
            .set("tasks", mutableListOf<Task>())
            .sample()

        When("존재하지 않은 프로젝트 id로 조회를 한 경우") {
            every { projectRepository.findByIdOrNull(any()) } returns null

            Then("Project NotFoundException 예외가 발생한다") {
                shouldThrow<NotFoundException> {
                    projectService.getProject(100L)
                }

                verify(exactly = 1) { projectRepository.findByIdOrNull(any()) }
            }
        }

        When("존재하는 프로젝트 id로 조회를 한 경우") {
            every { projectRepository.findByIdOrNull(any()) } returns existingProject

            Then("프로젝트가 성공적으로 조회된다") {
                val result = projectService.getProject(projectId)

                result.id shouldBe projectId
                result.name shouldBe projectName
                result.description shouldBe projectDescription
                result.owner shouldBe managerUser1.email

                verify(exactly = 1) { projectRepository.findByIdOrNull(any()) }
            }
        }
    }

    Given("관리자(ADMIN)가 프로젝트 생성 요청 시") {
        val projectName = "냥이프로젝트"
        val projectDescription = "new 설명"
        val tagIds = listOf(1L, 2L, 5L)

        val projectCreateRequest = sut.giveMeKotlinBuilder<ProjectCreateRequest>()
            .set("name", projectName)
            .set("description", projectDescription)
            .set("status", ProjectStatus.ACTIVE.name)
            .set("ownerId", managerUser1.id)
            .set("tags", tagIds)
            .sample()

        val tags = mutableSetOf("Backend", "Frontend")

        val savedProject = sut.giveMeKotlinBuilder<Project>()
            .setNotNull("id")
            .set("name", projectName)
            .set("description", projectDescription)
            .set("projectStatusWrapper", ProjectStatusWrapper(ProjectStatus.ACTIVE))
            .set("owner", managerUser1)
            .set("tags", tags)
            .set("workers", mutableListOf<Worker>())
            .set("tasks", mutableListOf<Task>())
            .sample()

        When("존재하지 않는 OwnerID로 요청하는 경우") {
            every { userRepository.findByIdOrNull(any()) } returns null

            Then("User NotFoundException 예외가 발생한다") {
                shouldThrow<NotFoundException> {
                    projectService.createProject(projectCreateRequest)
                }

                verify(exactly = 1) { userRepository.findByIdOrNull(any()) }
                verify(exactly = 0) { tagRepository.findById(any()) }
                verify(exactly = 0) { projectRepository.save(any()) }
            }
        }

        When("Worker를 Owner로 프로젝트를 생성할 경우") {
            every { userRepository.findByIdOrNull(any()) } returns workerUser
            every { projectRepository.save(any()) } returns savedProject

            Then("IllegalState Exception 예외가 발생한다") {
                shouldThrow<IllegalStateException> {
                    projectService.createProject(projectCreateRequest)
                }

                verify(exactly = 1) { userRepository.findByIdOrNull(any()) }
                verify(exactly = 0) { tagRepository.findById(any()) }
                verify(exactly = 0) { projectRepository.save(any()) }
            }
        }

        When("Manager를 Owner로 프로젝트를 생성할 경우") {
            every { userRepository.findByIdOrNull(managerUser1.id) } returns managerUser1
            every { projectRepository.save(any()) } returns savedProject
            every { tagRepository.findById(any()) } returns Optional.of(tag)

            Then("프로젝트가 성공적으로 생성된다") {
                val result = projectService.createProject(projectCreateRequest)

                result.name shouldBe projectName
                result.description shouldBe projectDescription
                result.status shouldBe ProjectStatus.ACTIVE.name
                result.owner shouldBe managerUser1.email
                result.tags shouldBe mutableSetOf("Backend", "Frontend")

                verify(exactly = 1) { userRepository.findByIdOrNull(managerUser1.id) }
                verify(exactly = 3) { tagRepository.findById(any()) }
                verify(exactly = 1) { projectRepository.save(any()) }
            }
        }
    }

    Given("프로젝트 정보 수정 요청 시") {
        val projectUpdateRequest = sut.giveMeKotlinBuilder<ProjectUpdateRequest>()
            .set("id", 10L)
            .set("description", "update description")
            .set("status", ProjectStatus.COMPLETED.name)
            .set("endDt", LocalDateTime.of(2026, 1, 1, 0, 0))
            .sample()

        val updatedProject = sut.giveMeKotlinBuilder<Project>()
            .set("id", 10L)
            .set("owner", managerUser1)
            .set("description", "update description")
            .set("endDt", LocalDateTime.of(2026, 1, 1, 0, 0))
            .set("projectStatusWrapper", ProjectStatusWrapper(ProjectStatus.COMPLETED))
            .set("workers", mutableListOf<Worker>())
            .set("tasks", mutableListOf<Task>())
            .set("tags", mutableSetOf("Backend", "Frontend"))
            .sample()

        When("수정할 정보를 받아서") {
            every { userRepository.findByIdOrNull(any()) } returns managerUser1
            // every { projectRepository.findByIdOrNull(any()) } returns mockk<Project>(relaxed = true)
            every { projectRepository.findByIdOrNull(any()) } returns updatedProject
            every { tagRepository.findById(any()) } returns Optional.of(tag)

            Then("프로젝트 정보가 성공적으로 수정된다") {
                val result = projectService.updateProject(1L, projectUpdateRequest)

                result.id shouldBe updatedProject.id
                result.description shouldBe updatedProject.description
                result.status shouldBe ProjectStatus.COMPLETED.name
                result.endDt shouldBe updatedProject.endDt
            }
        }
    }

    Given("프로젝트 책임자 변경 요청 시") {
        val oldProject = sut.giveMeKotlinBuilder<Project>()
            .setNotNull("id")
            .set("owner", managerUser1)
            .set("workers", mutableListOf<Worker>())
            .set("tasks", mutableListOf<Task>())
            .sample()

        val newProject = sut.giveMeKotlinBuilder<Project>()
            .setNotNull("id")
            .set("owner", managerUser2)
            .set("workers", mutableListOf<Worker>())
            .set("tasks", mutableListOf<Task>())
            .sample()

        When("변경할 담당자의 직책이 WORKER인 경우") {
            every { userRepository.findByIdOrNull(any()) } returns workerUser
            every { projectRepository.findByIdOrNull(any()) } returns oldProject

            Then("예외가 발생한다") {
                shouldThrow<IllegalStateException> {
                    projectService.updateProjectOwner(1L, workerUser.id)
                }

                verify(exactly = 1) { projectRepository.findByIdOrNull(any()) }
                verify(exactly = 1) { userRepository.findByIdOrNull(any()) }
            }
        }

        When("변경할 담당자의 직책이 MANAGER인 경우") {
            every { userRepository.findByIdOrNull(any()) } returns managerUser2
            every { projectRepository.findByIdOrNull(any()) } returns newProject

            Then("해당 프로젝트 책임자의 정보가 성공적으로 수정된다") {
                val result = projectService.updateProjectOwner(1L, managerUser2.id)

                result.owner shouldBe managerUser2.email
            }
        }
    }

    Given("프로젝트 삭제 요청 시") {

        When("존재하지 않는 프로젝트를 요청한 경우") {
            every { projectRepository.findByIdOrNull(any()) } returns null

            Then("Project NotFoundException 예외가 발생한다") {
                shouldThrow<NotFoundException> {
                    projectService.deleteProject(100L)
                }

                verify(exactly = 1) { projectRepository.findByIdOrNull(any()) }
            }
        }

        When("존재하는 프로젝트를 요청한 경우") {
            val mockProject = mockk<Project>(relaxed = true)

            every { projectRepository.findByIdOrNull(any()) } returns mockProject
            every { mockProject.softDelete() } just Runs

            Then("프로젝트 삭제에 성공한다") {
                projectService.deleteProject(1L)

                verify { projectRepository.findByIdOrNull(any()) }
                verify { mockProject.softDelete() }
            }
        }
    }
})