package com.example.googleclass.feature.course.data.mapper

import com.example.googleclass.feature.course.domain.model.Comment
import com.example.googleclass.feature.course.domain.model.Publication
import com.example.googleclass.feature.course.domain.model.PublicationType
import com.example.googleclass.feature.post.data.model.PostModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
    timeZone = java.util.TimeZone.getTimeZone("UTC")
}

fun PostModel.toPublication(): Publication {
    val title = text.lines().firstOrNull()?.take(80) ?: text.take(80).ifEmpty { "Публикация" }
    val createdAtDate = createdAt.parseOrNull() ?: Date(0)
    val deadlineDate = deadline
        ?.takeIf { it.isNotBlank() }?.parseOrNull()
    val maxScoreValue = if (maxScore <= 0) null else maxScore
    val files = this.files.map { it.fileName?.takeIf { n -> n.isNotBlank() } ?: "Файл" }
    val commentsList = comments.map { it.toComment() }
    return Publication(
        id = id,
        type = postType.toPublicationType(),
        title = title,
        text = text,
        authorId = author.id,
        createdAt = createdAtDate,
        deadline = deadlineDate,
        files = files.ifEmpty { null },
        comments = commentsList.ifEmpty { null },
        maxScore = maxScoreValue,
    )
}

private fun com.example.googleclass.feature.post.data.model.PostCommentModel.toComment(): Comment {
    val date = createdAt.parseOrNull() ?: Date(0)
    return Comment(userId = author.id, text = text, createdAt = date)
}

private fun com.example.googleclass.feature.post.data.model.PostType.toPublicationType(): PublicationType =
    when (this) {
        com.example.googleclass.feature.post.data.model.PostType.ANNOUNCEMENT -> PublicationType.ANNOUNCEMENT
        com.example.googleclass.feature.post.data.model.PostType.USEFUL_MATERIAL -> PublicationType.MATERIAL
        com.example.googleclass.feature.post.data.model.PostType.TASK -> PublicationType.ASSIGNMENT
    }

private fun String.parseOrNull(): Date? = try {
    isoFormat.parse(this)
} catch (_: Exception) {
    null
}
