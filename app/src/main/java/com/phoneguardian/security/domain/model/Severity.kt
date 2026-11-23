package com.phoneguardian.security.domain.model

enum class Severity(val weight: Int) {
    CRITICAL(40),
    HIGH(25),
    MEDIUM(15),
    LOW(5),
    INFO(0)
}
