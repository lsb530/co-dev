package com.boki.codev.service

import com.boki.codev.entity.project.Project
import com.boki.codev.entity.task.Task
import com.boki.codev.entity.worker.Worker
import com.boki.codev.fixture.adminUser
import com.boki.codev.fixture.managerUser1
import com.boki.codev.fixture.sut
import com.boki.codev.repository.ProjectRepository
import com.boki.codev.repository.TagRepository
import com.boki.codev.repository.UserRepository
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.context.TestSecurityContextHolder

class ProjectServiceTest : BehaviorSpec({
    val userRepository = mockk<UserRepository>()
    val projectRepository = mockk<ProjectRepository>()
    val tagRepository = mockk<TagRepository>()
    val projectService = ProjectService(userRepository, projectRepository, tagRepository)

    afterTest {
        clearAllMocks()
        TestSecurityContextHolder.clearContext()
    }

    Given("프로젝트 목록을 조회할 때") {
        When("인증정보가 있는(로그인을 한) 사용자가 ADMIN 권한인 경우") {
            val email = "admin@co-dev.com"

            val auth = TestingAuthenticationToken(email, "admin", "ROLE_ADMIN")
            TestSecurityContextHolder.getContext().authentication = auth

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

            every { userRepository.findUserByEmail(email) } returns adminUser
            every { projectRepository.findMyProjects(adminUser) } returns adminProjects
            every { projectRepository.findAll() } returns allProjects

            Then("모든 프로젝트 조회를 할 수 있다") {
                val result = projectService.getProjects()

                result.size shouldBe 3
                result[0].owner shouldBe email

                verify(exactly = 1) { projectRepository.findAll() }
                verify(exactly = 0) { userRepository.findUserByEmail(email) }
                verify(exactly = 0) { projectRepository.findMyProjects(adminUser) }
            }
        }

        When("인증정보가 있는(로그인을 한) 사용자가 MANAGER 권한인 경우") {
            val email = "manager1@co-dev.com"

            val auth = TestingAuthenticationToken(email, "manager", "ROLE_MANAGER")
            TestSecurityContextHolder.getContext().authentication = auth

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

            every { userRepository.findUserByEmail(email) } returns managerUser1
            every { projectRepository.findMyProjects(managerUser1) } returns managerProjects
            every { projectRepository.findAll() } returns allProjects

            Then("자신의 소유인 프로젝트 조회를 할 수 있다") {
                val result = projectService.getProjects()

                result.size shouldBe 2
                result[0].owner shouldBe email

                verify(exactly = 0) { projectRepository.findAll() }
                verify(exactly = 1) { userRepository.findUserByEmail(email) }
                verify(exactly = 1) { projectRepository.findMyProjects(managerUser1) }
            }
        }

        When("인증정보가 없는(로그인을 하지 않은) 요청일 경우") {
            val anonymousAuth = AnonymousAuthenticationToken(
                "anonymous",
                "anonymousUser",
                listOf(SimpleGrantedAuthority("ROLE_ANONYMOUS"))
            )
            TestSecurityContextHolder.getContext().authentication = anonymousAuth

            Then("프로젝트 목록 조회를 할 수 없다") {
                every { userRepository.findUserByEmail(any()) } returns null

                shouldThrow<NoSuchElementException> {
                    projectService.getProjects()
                }

                verify(exactly = 1) { userRepository.findUserByEmail(any()) }
                verify(exactly = 0) { projectRepository.findMyProjects(any()) }
            }
        }
    }
})