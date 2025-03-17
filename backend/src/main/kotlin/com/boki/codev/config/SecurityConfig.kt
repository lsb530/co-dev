package com.boki.codev.config

import com.boki.codev.filter.JwtAuthenticationFilter
import com.boki.codev.filter.JwtVerificationFilter
import com.boki.codev.filter.MDCLoggingFilter
import com.boki.codev.security.RestAccessDeniedHandler
import com.boki.codev.security.RestAuthenticationEntryPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@EnableMethodSecurity
@EnableWebSecurity
@Configuration
class SecurityConfig {
    @Bean
    fun encoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtVerificationFilter: JwtVerificationFilter,
        jwtAuthenticationFilter: JwtAuthenticationFilter,
        mdcLoggingFilter: MDCLoggingFilter,
        restAuthenticationEntryPoint: RestAuthenticationEntryPoint,
        restAccessDeniedHandler: RestAccessDeniedHandler,
    ): DefaultSecurityFilterChain {
        http
            .httpBasic { it.disable() }
            .csrf { it.disable() }
            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint(restAuthenticationEntryPoint)
                    .accessDeniedHandler(restAccessDeniedHandler)
            }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/api/v1/auth/**", "/error")
                    .permitAll()
                    .anyRequest()
                    .fullyAuthenticated()
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(jwtVerificationFilter, jwtAuthenticationFilter::class.java)
            .addFilterBefore(mdcLoggingFilter, jwtVerificationFilter::class.java)
        return http.build()
    }
}