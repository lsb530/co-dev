package com.boki.codev.util

import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.Inet4Address
import java.net.InetAddress

private val logger = KotlinLogging.logger {}

object IPUtil {
    fun convertV4IP(ip: String?): String? {
        if (ip == null) return null
        try {
            val inetAddress = InetAddress.getByName(ip)
            if (inetAddress is Inet4Address) {
                return inetAddress.hostAddress
            }
            // IPv6 주소 중 IPv4 매핑 주소인 경우 ::ffff:192.168.0.1 형식일 때 변환
            if (ip.startsWith("::ffff:")) {
                return ip.removePrefix("::ffff:")
            }
            if (ip == "::1" || ip == "0:0:0:0:0:0:0:1") {
                return "127.0.0.1"
            }
        } catch (e: Exception) {
            logger.error { "Could not convert IPv4 from $ip" }
        }
        return ip
    }
}