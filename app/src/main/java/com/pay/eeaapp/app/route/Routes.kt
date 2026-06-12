package com.pay.eeaapp.app.route

object Routes {
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val PROPONENT_DASHBOARD = "proponent_dashboard"
    const val APPLY_PROJECT = "apply_project"
    const val PROJECT_DETAIL = "project_detail/{projectId}"
    fun projectDetail(projectId: String) = "project_detail/$projectId"
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_REVIEW = "admin_review/{projectId}"
    fun adminReview(projectId: String) = "admin_review/$projectId"
    const val ANALYTICS = "analytics"
    const val MAP = "map"
}