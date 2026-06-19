package com.pay.eeaapp.ui.proponent.state

import android.net.Uri
import com.pay.eeaapp.domain.models.Project
import com.pay.eeaapp.domain.models.ProjectDocument
import com.pay.eeaapp.domain.models.ReviewComment

data class ProjectDetailUiState(
    val project: Project? = null,
    val documents: List<ProjectDocument> = emptyList(),
    val reviews: List<ReviewComment> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val resubmitState: SubmitState = SubmitState.Idle,
    val pickedResubmitFiles: List<Pair<Uri, String>> = emptyList()
)