package com.example.googleclass.feature.taskdetail.data.mapper

import com.example.googleclass.feature.taskdetail.data.model.CommentDto
import com.example.googleclass.feature.taskdetail.domain.model.Comment
import com.example.googleclass.feature.taskdetail.studentchat.domain.model.ChatMessage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

fun CommentDto.toComment(): Comment = Comment(
    id = id,
    authorName = "${author.firstName.orEmpty()} ${author.lastName.orEmpty()}".trim(),
    text = text,
    createdAt = formatCommentDate(createdAt),
)

fun CommentDto.toChatMessage(currentUserId: String): ChatMessage = ChatMessage(
    id = id,
    text = text,
    authorId = author.id,
    authorName = "${author.firstName.orEmpty()} ${author.lastName.orEmpty()}".trim(),
    createdAt = formatCommentDate(createdAt),
    isFromTeacher = author.id == currentUserId,
)

private val isoFormats = listOf(
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
)

private fun formatCommentDate(isoString: String): String {
    val cleaned = isoString.trim()
    for (fmt in isoFormats) {
        try {
            val date = fmt.parse(cleaned) ?: continue
            val now = Calendar.getInstance()
            val cal = Calendar.getInstance().apply { time = date }
            val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
            return when {
                now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                        now.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR) ->
                    "Сегодня ${timeFmt.format(date)}"

                now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                        now.get(Calendar.DAY_OF_YEAR) - cal.get(Calendar.DAY_OF_YEAR) == 1 ->
                    "Вчера ${timeFmt.format(date)}"

                else -> {
                    val dateFmt = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
                    dateFmt.format(date)
                }
            }
        } catch (_: Exception) {
            continue
        }
    }
    return isoString
}
