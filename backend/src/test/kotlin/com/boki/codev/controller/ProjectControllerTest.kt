package com.boki.codev.controller

import com.boki.codev.dto.OwnerChangeRequest
import com.boki.codev.dto.ProjectCreateRequest
import com.boki.codev.dto.ProjectSimpleResponse
import com.boki.codev.dto.ProjectUpdateRequest
import com.boki.codev.entity.user.Role
import com.boki.codev.security.AuthenticationRequest
import com.boki.codev.security.AuthenticationResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestClient
import java.io.InputStreamReader
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
    @DisplayName("인증되지 않은 사용자는 프로젝트 목록 조회를 할 수 없다")
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
    @DisplayName("인증된 사용자는 프로젝트 목록 조회를 할 수 있다")
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
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 프로젝트 단건 조회를 할 수 없다")
    fun shouldFailedGetProjectWithoutAuth() {
        // given
        val projectId = 4L

        // when
        val result = restClient.get()
            .uri("/api/v1/projects/$projectId")
            .exchange { _, response ->
                val body = response.bodyTo(String::class.java)
                Pair(response.statusCode, body)
            }

        // then
        assertThat(result?.first).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result?.second).contains("인증(로그인)이 필요합니다")
    }

    @Test
    @DisplayName("관리자(ADMIN)는 프로젝트 단건 조회를 할 수 있다")
    fun shouldSuccessGetProjectWithAdmin() {
        // given
        val projectId = 4L
        val accessToken = login(Role.ADMIN)
        val responseType = ProjectSimpleResponse::class.java

        // when
        val result = restClient.get()
            .uri("/api/v1/projects/$projectId")
            .header("Authorization", "Bearer $accessToken")
            .exchange { _, response ->
                val body = response.bodyTo(responseType)
                StatusAndResponse(response.statusCode, body!!)
            }!!

        // then
        assertThat(result.statusCode).isEqualTo(HttpStatus.OK)

        val foundProject = result.response as ProjectSimpleResponse
        assertThat(foundProject.name).isEqualTo("project4")
    }

    @Test
    @DisplayName("프로젝트 소유자(Owner)가 아닌 사람은 프로젝트 단건 조회를 할 수 없다")
    fun shouldSuccessGetProjectWithAnother() {
        // given
        val projectId = 4L
        val accessToken = login(Role.MANAGER, 2)

        // when
        restClient.get()
            .uri("/api/v1/projects/$projectId")
            .header("Authorization", "Bearer $accessToken")
            .exchange { _, response ->
                // then
                assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
            }
    }

    @Test
    @DisplayName("프로젝트 소유자(Owner)는 프로젝트 단건 조회를 할 수 있다")
    fun shouldSuccessGetProjectWithOwner() {
        // given
        val projectId = 4L
        val accessToken = login(Role.MANAGER)
        val responseType = ProjectSimpleResponse::class.java

        // when
        val result = restClient.get()
            .uri("/api/v1/projects/$projectId")
            .header("Authorization", "Bearer $accessToken")
            .exchange { _, response ->
                val body = response.bodyTo(responseType)
                StatusAndResponse(response.statusCode, body!!)
            }!!

        // then
        assertThat(result.statusCode).isEqualTo(HttpStatus.OK)

        val foundProject = result.response as ProjectSimpleResponse
        assertThat(foundProject.name).isEqualTo("project4")
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
    @DisplayName("관리자(ADMIN)가 아닌 사용자는 프로젝트를 생성할 수 없다")
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
    @DisplayName("관리자(ADMIN)는 프로젝트를 생성할 수 있다")
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

    /**
     * 수정(PATCH or PUT)
     */
    @Test
    @DisplayName("노동자(WORKER)는 프로젝트 수정을 할 수 없다")
    fun shouldFailedUpdateProjectWithWorker() {
        // given
        val accessToken = login(Role.WORKER)
        val updateProjectId = 4L
        val now = LocalDateTime.now()
        val lastDayOfThisYear = LocalDateTime.of(
            now.year.plus(1), 1, 1, 0, 0, 0
        ).minusDays(1)

        val projectUpdateRequest = ProjectUpdateRequest(
            description = "UPDATE Description",
            status = "BACKLOG",
            endDt = lastDayOfThisYear,
            tags = listOf(1, 2, 5, 6)
        )

        // when
        restClient.patch()
            .uri("/api/v1/projects/$updateProjectId")
            .header("Authorization", "Bearer $accessToken")
            .body(projectUpdateRequest)
            .contentType(MediaType.APPLICATION_JSON)
            .exchange { _, response ->
                // then
                assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
            }
    }

    @Test
    @DisplayName("프로젝트 소유자(Owner)가 아닌 사용자는 프로젝트 수정 요청을 할 수 없다")
    fun shouldFailedUpdateProjectWithoutOwner() {
        // given
        val accessToken = login(Role.MANAGER, 2)
        val updateProjectId = 4L
        val now = LocalDateTime.now()
        val lastDayOfThisYear = LocalDateTime.of(
            now.year.plus(1), 1, 1, 0, 0, 0
        ).minusDays(1)

        val projectUpdateRequest = ProjectUpdateRequest(
            description = "UPDATE Description",
            status = "BACKLOG",
            endDt = lastDayOfThisYear,
            tags = listOf(1, 2, 5, 6)
        )

        // when
        restClient.patch()
            .uri("/api/v1/projects/$updateProjectId")
            .header("Authorization", "Bearer $accessToken")
            .body(projectUpdateRequest)
            .contentType(MediaType.APPLICATION_JSON)
            .exchange { _, response ->
                // then
                assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
            }
    }

    @Test
    @DisplayName("관리자(ADMIN)는 프로젝트 정보를 수정할 수 있다")
    fun shouldSuccessUpdateProjectWithAdmin() {
        // given
        val accessToken = login(Role.ADMIN)
        val updateProjectId = 4L
        val now = LocalDateTime.now()
        val lastDayOfThisYear = LocalDateTime.of(
            now.year.plus(1), 1, 1, 0, 0, 0
        ).minusDays(1)

        val projectUpdateRequest = ProjectUpdateRequest(
            description = "UPDATE Description",
            status = "BACKLOG",
            endDt = lastDayOfThisYear,
            tags = listOf(1, 2, 5, 6)
        )

        val responseType = ProjectSimpleResponse::class.java

        // when
        val result = restClient.patch()
            .uri("/api/v1/projects/$updateProjectId")
            .header("Authorization", "Bearer $accessToken")
            .body(projectUpdateRequest)
            .contentType(MediaType.APPLICATION_JSON)
            .exchange { _, response ->
                val body = response.bodyTo(responseType)
                StatusAndResponse(response.statusCode, body!!)
            }!!

        // then
        assertThat(result.statusCode).isEqualTo(HttpStatus.OK)

        val updatedProject = result.response as ProjectSimpleResponse
        assertThat(updatedProject.description).isEqualTo("UPDATE Description")
        assertThat(updatedProject.status).isEqualTo("BACKLOG")
        assertThat(updatedProject.tags).containsExactlyInAnyOrder("Backend", "Frontend")
    }

    @Test
    @DisplayName("프로젝트 소유자(Owner)는 프로젝트 정보를 수정할 수 있다")
    fun shouldSuccessUpdateProjectWithOwner() {
        // given
        val accessToken = login(Role.MANAGER, 1)
        val updateProjectId = 4L
        val now = LocalDateTime.now()
        val lastDayOfThisYear = LocalDateTime.of(
            now.year.plus(1), 1, 1, 0, 0, 0
        ).minusDays(1)

        val projectUpdateRequest = ProjectUpdateRequest(
            description = "UPDATE Description",
            status = "BACKLOG",
            endDt = lastDayOfThisYear,
            tags = listOf(1, 2, 5, 6)
        )

        val responseType = ProjectSimpleResponse::class.java

        // when
        val result = restClient.patch()
            .uri("/api/v1/projects/$updateProjectId")
            .header("Authorization", "Bearer $accessToken")
            .body(projectUpdateRequest)
            .contentType(MediaType.APPLICATION_JSON)
            .exchange { _, response ->
                val body = response.bodyTo(responseType)
                StatusAndResponse(response.statusCode, body!!)
            }!!

        // then
        assertThat(result.statusCode).isEqualTo(HttpStatus.OK)

        val updatedProject = result.response as ProjectSimpleResponse
        assertThat(updatedProject.description).isEqualTo("UPDATE Description")
        assertThat(updatedProject.status).isEqualTo("BACKLOG")
        assertThat(updatedProject.tags).containsExactlyInAnyOrder("Backend", "Frontend")
    }

    @ParameterizedTest
    @CsvSource(
        "MANAGER, 403",
        "WORKER, 403",
        "ANONYMOUS, 401"
    )
    @DisplayName("관리자(ADMIN)가 아닌 사용자는 프로젝트 담당자를 바꿀 수 없다")
    fun shouldFailedChangeProjectOwnerWithoutAdmin(roleStr: String, expectedStatusCode: Int) {
        // given
        val accessToken = if (roleStr == "ANONYMOUS") null else login(Role.valueOf(roleStr))
        val updateProjectId = 3L
        val ownerChangeRequest = OwnerChangeRequest(ownerId = 3L)

        // when
        restClient.patch()
            .uri("/api/v1/projects/$updateProjectId/owner")
            .header("Authorization", "Bearer $accessToken")
            .body(ownerChangeRequest)
            .contentType(MediaType.APPLICATION_JSON)
            .exchange { _, response ->
                // then
                if (accessToken == null) {
                    assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
                } else {
                    assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
                }
            }
    }

    @Test
    @DisplayName("관리자(ADMIN)는 프로젝트 담당자를 바꿀 수 있다")
    fun shouldSuccessChangeProjectOwnerWithAdmin() {
        // given
        val accessToken = login(Role.ADMIN)
        val updateProjectId = 3L
        val ownerChangeRequest = OwnerChangeRequest(ownerId = null)

        val responseType = ProjectSimpleResponse::class.java

        // when
        val result = restClient.patch()
            .uri("/api/v1/projects/$updateProjectId/owner")
            .header("Authorization", "Bearer $accessToken")
            .body(ownerChangeRequest)
            .contentType(MediaType.APPLICATION_JSON)
            .exchange { _, response ->
                val body = response.bodyTo(responseType)
                StatusAndResponse(response.statusCode, body!!)
            }

        // then
        assertThat(result?.statusCode).isEqualTo(HttpStatus.OK)

        val updatedProject = result?.response as ProjectSimpleResponse
        assertThat(updatedProject.owner).isEqualTo(null)
    }

    /**
     * 삭제(DELETE)
     */
    @ParameterizedTest
    @CsvSource(
        "MANAGER, 403",
        "WORKER, 403",
        "ANONYMOUS, 401"
    )
    @DisplayName("관리자(ADMIN)가 아닌 사용자는 프로젝트를 삭제할 수 없다")
    fun shouldFailedDeleteProjectOwnerWithoutAdmin(roleStr: String, expectedStatusCode: Int) {
        // given
        val accessToken = if (roleStr == "ANONYMOUS") null else login(Role.valueOf(roleStr))
        val deleteProjectId = 1L

        // when
        restClient.delete()
            .uri("/api/v1/projects/$deleteProjectId")
            .header("Authorization", "Bearer $accessToken")
            .exchange { _, response ->
                // then
                if (accessToken == null) {
                    assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
                } else {
                    assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
                }
            }
    }

    @Transactional
    @Test
    @DisplayName("관리자(ADMIN)는 프로젝트를 삭제할 수 있다")
    fun shouldSuccessDeleteProjectOwnerWithAdmin() {
        // given
        val accessToken = login(Role.ADMIN)
        val deleteProjectId = 1L

        // when
        restClient.delete()
            .uri("/api/v1/projects/$deleteProjectId")
            .header("Authorization", "Bearer $accessToken")
            .exchange { _, response ->
                // then
                assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
            }

        restClient.get()
            .uri("/api/v1/projects/$deleteProjectId")
            .header("Authorization", "Bearer $accessToken")
            .exchange { _, response ->
                // then
                assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
                InputStreamReader(response.body).use {
                    val responseBody = jacksonObjectMapper().readTree(it)
                    assertThat(responseBody["code"].asText()).hasToString("Not Found")
                }
            }
    }

    private fun login(role: Role, order: Int? = 1): String {
        val authRequest = when (role) {
            Role.ADMIN -> AuthenticationRequest("admin@co-dev.com", "admin")
            Role.MANAGER -> {
                AuthenticationRequest("manager${order}@co-dev.com", "manager")
            }
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

