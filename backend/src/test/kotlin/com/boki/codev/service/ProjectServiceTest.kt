package com.boki.codev.service

import com.boki.codev.entity.project.Project
import com.boki.codev.entity.task.Task
import com.boki.codev.entity.worker.Worker
import com.boki.codev.fixture.adminUser
import com.boki.codev.fixture.sut
import com.boki.codev.repository.ProjectRepository
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
    val projectService = ProjectService(userRepository, projectRepository)

    afterTest {
        clearAllMocks()
        TestSecurityContextHolder.clearContext()
    }

    Given("프로젝트 목록을 조회할 때") {
        When("인증정보가 있는(로그인을 한) 사용자일 경우") {
            val email = "admin@co-dev.com"

            val auth = TestingAuthenticationToken(email, "admin", "ROLE_ADMIN")
            TestSecurityContextHolder.getContext().authentication = auth

            val projects = sut.giveMeKotlinBuilder<Project>()
                .setNotNull("id")
                .set("owner", adminUser)
                .set("workers", mutableListOf<Worker>())
                .set("tasks", mutableListOf<Task>())
                .sampleList(3)

            every { userRepository.findUserByEmail(email) } returns adminUser
            every { projectRepository.findMyProjects(adminUser) } returns projects

            Then("프로젝트 목록 조회를 할 수 있다") {
                val result = projectService.getProjects()
                println(result)

                result.size shouldBe 3
                result[0].owner shouldBe email

                verify(exactly = 1) { userRepository.findUserByEmail(email) }
                verify(exactly = 1) { projectRepository.findMyProjects(adminUser) }
            }
        }

        When("인증정보가 없는(로그인을 하지 않은) 사용자일 경우") {
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