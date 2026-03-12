package com.example.googleclass.feature.taskdetail.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.alpha
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.googleclass.R
import com.example.googleclass.common.presentation.component.CommentUiModel
import com.example.googleclass.common.presentation.component.CommentsSection
import com.example.googleclass.feature.taskdetail.domain.model.Comment

@Composable
internal fun StudentCommentsSection(
    selectedTab: StudentTab,
    publicComments: List<Comment>,
    privateComments: List<Comment>,
    commentInput: String,
    hasPrivateCommentsAccess: Boolean = true,
    onEvent: (TaskDetailUiEvent) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                Tab(
                    selected = selectedTab == StudentTab.PUBLIC_COMMENTS,
                    onClick = { onEvent(TaskDetailUiEvent.StudentTabSelected(StudentTab.PUBLIC_COMMENTS)) },
                    text = {
                        Text(
                            text = stringResource(R.string.public_comments),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    },
                )
                Tab(
                    selected = selectedTab == StudentTab.PRIVATE_COMMENTS,
                    onClick = {
                        if (hasPrivateCommentsAccess) {
                            onEvent(TaskDetailUiEvent.StudentTabSelected(StudentTab.PRIVATE_COMMENTS))
                        }
                    },
                    modifier = Modifier.alpha(if (hasPrivateCommentsAccess) 1f else 0.5f),
                    text = {
                        Text(
                            text = stringResource(R.string.private_comments),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    },
                )
            }

            val comments = when (selectedTab) {
                StudentTab.PUBLIC_COMMENTS -> publicComments
                StudentTab.PRIVATE_COMMENTS -> privateComments
            }

            val placeholder = when (selectedTab) {
                StudentTab.PUBLIC_COMMENTS -> stringResource(R.string.add_comment_placeholder)
                StudentTab.PRIVATE_COMMENTS -> stringResource(R.string.add_private_comment_placeholder)
            }

            CommentsSection(
                comments = comments.map { it.toUiModel() },
                inputValue = commentInput,
                placeholder = placeholder,
                onInputChange = { onEvent(TaskDetailUiEvent.CommentInputChanged(it)) },
                onSend = { onEvent(TaskDetailUiEvent.SendComment) },
            )
        }
    }
}

internal fun Comment.toUiModel() = CommentUiModel(
    id = id,
    authorName = authorName,
    text = text,
    createdAt = createdAt,
)
