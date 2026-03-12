package com.example.googleclass.common.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.googleclass.R
import com.example.googleclass.common.presentation.theme.MediumGray
import com.example.googleclass.common.presentation.theme.PrimaryBlue

data class CommentUiModel(
    val id: String,
    val authorName: String,
    val text: String,
    val createdAt: String,
)

@Composable
fun CommentsSection(
    comments: List<CommentUiModel>,
    inputValue: String,
    placeholder: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        CommentsList(comments = comments)
        CommentInputField(
            value = inputValue,
            placeholder = placeholder,
            onValueChange = onInputChange,
            onSend = onSend,
        )
    }
}

@Composable
fun CommentsList(
    comments: List<CommentUiModel>,
    modifier: Modifier = Modifier,
) {
    if (comments.isEmpty()) {
        Text(
            text = stringResource(R.string.no_comments),
            style = MaterialTheme.typography.bodyMedium,
            color = MediumGray,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
        )
    } else {
        Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            comments.forEach { comment ->
                CommentItem(comment = comment)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun CommentItem(comment: CommentUiModel) {
    Column {
        Text(
            text = comment.authorName,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = comment.text,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = comment.createdAt,
            style = MaterialTheme.typography.labelMedium,
            color = MediumGray,
        )
    }
}

@Composable
fun CommentInputField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediumGray,
                )
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
            ),
            textStyle = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onSend,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = PrimaryBlue,
                contentColor = Color.White,
            ),
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(R.string.send_comment),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
