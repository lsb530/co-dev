package com.boki.codev.controller

import com.boki.codev.dto.ProjectCreateRequest
import com.boki.codev.dto.ProjectSimpleResponse
import com.boki.codev.entity.user.Role
import com.boki.codev.security.AuthenticationRequest
import com.boki.codev.security.AuthenticationResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestClient
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProjectControllerTest {

    data class StatusAndResponse(
        val statusCode: HttpStatusCode,
        val response: Any
    )

    @LocalServerPort
    private var port: Int = 0

    private val baseUrl: String
        get() = "http://localhost:$port"

    private val restClient: RestClient
        get() = RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
            .build()

    /**
     * 조회(GET)
     */
    @Test
    @DisplayName("인증되지 않은 사용자는 프로젝트 목록 조회에 실패한다")
    fun shouldFailedGetProjectsWithoutAuth() {
        // when
        val result = restClient.get()
            .uri("/api/v1/projects")
            .exchange { _, response ->
                val body = response.bodyTo(String::class.java)
                Pair(response.statusCode, body)
            }

        // then
        assertThat(result?.first).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result?.second).contains("인증(로그인)이 필요합니다")
    }

    @Test
    @DisplayName("인증된 사용자는 프로젝트 목록 조회에 성공한다")
    fun shouldSuccessGetProjectsWithAuth() {
        // given
        val accessToken = login(Role.ADMIN)
        val responseType = object : ParameterizedTypeReference<List<ProjectSimpleResponse>>() {}

        // when
        val result = restClient.get()
            .uri("/api/v1/projects")
            .header("Authorization", "Bearer $accessToken")
            .exchange { _, response ->
                val body = response.bodyTo(responseType)
                StatusAndResponse(response.statusCode, body!!)
            }!!

        // then
        assertThat(result.statusCode).isEqualTo(HttpStatus.OK)

        val projects = result.response as List<ProjectSimpleResponse>
        assertThat(projects).isNotEmpty()
        assertThat(projects[0].name).isEqualTo("project1")
        assertThat(projects[0].status).isEqualTo("ACTIVE")
        assertThat(projects[0].tags).containsExactlyInAnyOrder("Backend", "Frontend", "Devops")
    }

    /**
     * 추가(POST)
     */
    @ParameterizedTest
    @CsvSource(
        "MANAGER, 403",
        "WORKER, 403",
        "ANONYMOUS, 401"
    )
    @DisplayName("관리자(ADMIN)가 아닌 사용자의 프로젝트 생성 요청은 실패한다")
    fun shouldFailedCreateProjectWithoutAdmin(roleStr: String, expectedStatusCode: Int) {
        // given
        val accessToken = if (roleStr == "ANONYMOUS") null else login(Role.valueOf(roleStr))
        val projectCreateRequest = ProjectCreateRequest(
            name = "New Project",
            description = "New Description",
            status = "ACTIVE",
            startDt = LocalDateTime.now().plusDays(1),
            endDt = LocalDateTime.now().plusDays(10),
            ownerId = 2L,
            tags = listOf(1, 2, 5, 6, 3, 4)
        )

        // when
        restClient.post()
            .uri("/api/v1/projects")
            .header("Authorization", "Bearer $accessToken")
            .body(projectCreateRequest)
            .contentType(MediaType.APPLICATION_JSON)
            .exchange { _, response ->
                // then
                if (accessToken == null) {
                    assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
                } else {
                    assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
                }
            }!!
    }

    @Test
    @DisplayName("관리자(ADMIN)는 프로젝트 생성에 성공한다")
    fun shouldSuccessCreateProjectWithAdmin() {
        // given
        val accessToken = login(Role.ADMIN)
        val projectCreateRequest = ProjectCreateRequest(
            name = "New Project",
            description = "New Description",
            status = "ACTIVE",
            startDt = LocalDateTime.now().plusDays(1),
            endDt = LocalDateTime.now().plusDays(10),
            ownerId = 2L,
            tags = listOf(1, 2, 5, 6, 3, 4)
        )
        val responseType = ProjectSimpleResponse::class.java

        // when
        val result = restClient.post()
            .uri("/api/v1/projects")
            .header("Authorization", "Bearer $accessToken")
            .body(projectCreateRequest)
            .contentType(MediaType.APPLICATION_JSON)
            .exchange { _, response ->
                val body = response.bodyTo(responseType)
                StatusAndResponse(response.statusCode, body!!)
            }!!

        // then
        assertThat(result.statusCode).isEqualTo(HttpStatus.CREATED)

        val createdProject = result.response as ProjectSimpleResponse
        assertThat(createdProject.name).isEqualTo("New Project")
        assertThat(createdProject.description).isEqualTo("New Description")
        assertThat(createdProject.status).isEqualTo("ACTIVE")
        assertThat(createdProject.tags).containsExactlyInAnyOrder("Backend", "Frontend", "Mobile", "Devops")
    }


    private fun login(role: Role): String {
        val authRequest = when (role) {
            Role.ADMIN -> AuthenticationRequest("admin@co-dev.com", "admin")
            Role.MANAGER -> AuthenticationRequest("manager1@co-dev.com", "manager")
            Role.WORKER -> AuthenticationRequest("worker@co-dev.com", "worker")
        }

        val result = restClient.post()
            .uri("/api/v1/auth")
            .body(authRequest)
            .exchange { _, response ->
                StatusAndResponse(response.statusCode, response.bodyTo(AuthenticationResponse::class.java)!!)
            }!!

        if (!result.statusCode.is2xxSuccessful) {
            throw RuntimeException("로그인 실패")
        }

        return (result.response as AuthenticationResponse).accessToken
    }

}

