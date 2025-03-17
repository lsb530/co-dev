package com.boki.codev.entity.httpinterface

import com.boki.codev.util.IPUtil
import jakarta.persistence.*
import jakarta.servlet.http.HttpServletRequest
import java.time.LocalDateTime

@Entity
class HttpInterface(
    val scheme: String?,
    val serverName: String?,
    val serverPort: Int?,
    val method: String?,
    val locale: String?,
    val protocol: String?,
    val cookies: String?,
    val origin: String?,
    val referrer: String?,
    val userAgent: String?,
    val requestUri: String?,
    val localAddr: String?,
    val remoteAddr: String?,
    val remoteHost: String?,
    @Column(name = "x-forwarded-for")
    val forwardedFor: String?,
    @Column(name = "x-real-ip")
    val realIp: String?,
    val createdAt: LocalDateTime,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {
    constructor(it: HttpServletRequest) : this(
        scheme = it.scheme,
        serverName = it.serverName,
        serverPort = it.serverPort,
        method = it.method,
        locale = it.locale.toString(),
        protocol = it.protocol,
        cookies = it.cookies
            ?.map { cookie -> "${cookie.name}:${cookie.value}" }
            .toString(),
        origin = it.getHeader("Origin"),
        referrer = it.getHeader("referer"),
        userAgent = it.getHeader("user-agent"),
        requestUri = it.requestURI,
        localAddr = IPUtil.convertV4IP(it.localAddr),
        remoteAddr = IPUtil.convertV4IP(it.remoteAddr),
        remoteHost = IPUtil.convertV4IP(it.remoteHost),
        forwardedFor = it.getHeader("x-forwarded-for"),
        realIp = it.getHeader("x-real-ip"),
        createdAt = LocalDateTime.now(),
    )
}