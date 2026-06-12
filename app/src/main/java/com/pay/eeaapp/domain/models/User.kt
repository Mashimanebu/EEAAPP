package com.pay.eeaapp.domain.models

enum class UserRole { ADMIN, PROPONENT }

enum class ProjectStatus(val label: String, val colorHex: Long) {
    DRAFT("Draft", 0xFF90A4AE), SUBMITTED("Submitted", 0xFF42A5F5), UNDER_REVIEW(
        "Under Review", 0xFFFFA726
    ),
    AMENDMENTS_REQUIRED("Amendments Required", 0xFFEF5350), APPROVED(
        "Approved", 0xFF66BB6A
    ),
    REJECTED("Rejected", 0xFFB71C1C);

    companion object {
        fun fromName(name: String): ProjectStatus =
            entries.firstOrNull { it.name == name } ?: SUBMITTED
    }
}

data class User(
    val uid: String,
    val fullName: String,
    val email: String,
    val company: String? = null,
    val role: UserRole
)







