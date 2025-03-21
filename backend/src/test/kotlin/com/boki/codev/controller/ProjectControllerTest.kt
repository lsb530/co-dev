package com.boki.codev.controller

import com.boki.codev.dto.ProjectSimpleResponse
import com.boki.codev.security.AuthenticationRequest
import com.boki.codev.security.AuthenticationResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestClient

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
        // when
        val accessToken = login()
        val responseType = object : ParameterizedTypeReference<List<ProjectSimpleResponse>>() {}

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


    private fun login(): String {
        val authRequest = AuthenticationRequest(
            email = "admin@co-dev.com",
            password = "admin"
        )

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

