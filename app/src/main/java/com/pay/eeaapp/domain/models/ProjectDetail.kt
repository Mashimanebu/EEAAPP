package com.pay.eeaapp.domain.models

data class ProjectDetail(
    val project: Project,
    val documents: List<ProjectDocument>,
    val reviews: List<ReviewComment>
)