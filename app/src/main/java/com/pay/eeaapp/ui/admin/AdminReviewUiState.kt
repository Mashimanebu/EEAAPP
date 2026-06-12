package com.pay.eeaapp.ui.admin

import android.net.Uri
import com.pay.eeaapp.domain.models.Project
import com.pay.eeaapp.domain.models.ProjectDocument
import com.pay.eeaapp.domain.models.ReviewComment

data class AdminReviewUiState(
    val project: Project? = null,
    val documents: List<ProjectDocument> = emptyList(),
    val reviews: List<ReviewComment> = emptyList(),
    val isLoading: Boolean = true,
    val comment: String = "",
    val attachments: List<Pair<Uri, String>> = emptyList(),
    val actionState: ReviewAction = ReviewAction.Idle
)
