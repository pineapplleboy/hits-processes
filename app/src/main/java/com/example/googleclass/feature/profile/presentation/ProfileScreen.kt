package com.example.googleclass.feature.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.googleclass.R
import com.example.googleclass.common.presentation.components.ClassroomTopAppBar
import com.example.googleclass.common.presentation.theme.ErrorRed
import com.example.googleclass.common.presentation.theme.GoogleClassTheme
import com.example.googleclass.common.presentation.theme.PrimaryBlue
import com.example.googleclass.common.presentation.theme.SecondaryText
import com.example.googleclass.common.presentation.theme.White
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onArchivedCoursesClick: () -> Unit,
    onSwitchUserClick: () -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    LaunchedEffect(viewModel.logoutCompleted) {
        if (viewModel.logoutCompleted) onLogoutClick()
    }

    ProfileScreenContent(
        state = viewModel.state,
        onNavigateBack = onNavigateBack,
        onArchivedCoursesClick = onArchivedCoursesClick,
        onLogoutClick = viewModel::logout,
    )
}

@Composable
fun ProfileScreenContent(
    state: ProfileScreenState,
    onNavigateBack: () -> Unit,
    onArchivedCoursesClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ClassroomTopAppBar(
                title = stringResource(R.string.profile),
                onNavigateBack = onNavigateBack,
            )
        },
    ) { padding ->
        when (state) {
            is ProfileScreenState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is ProfileScreenState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = ErrorRed,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            is ProfileScreenState.Content -> {
                ProfileContent(
                    state = state,
                    onArchivedCoursesClick = onArchivedCoursesClick,
                    onLogoutClick = onLogoutClick,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileScreenState.Content,
    onArchivedCoursesClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        ProfileHeader(
            firstName = state.firstName,
            lastName = state.lastName,
        )

        Spacer(modifier = Modifier.height(24.dp))

        ProfileInfoCard(
            email = state.email,
            city = state.city,
            birthday = state.birthday,
        )

        Spacer(modifier = Modifier.height(16.dp))

        ProfileActionItem(
            iconRes = R.drawable.ic_archive,
            text = stringResource(R.string.profile_archived_courses),
            onClick = onArchivedCoursesClick,
        )

        ProfileActionItem(
            iconRes = R.drawable.ic_logout,
            text = stringResource(R.string.logout_action),
            onClick = onLogoutClick,
            tint = MaterialTheme.colorScheme.error,
            textColor = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun ProfileHeader(
    firstName: String,
    lastName: String,
    modifier: Modifier = Modifier,
) {
    val fullName = "$lastName $firstName"
    val initials = fullName.split(" ")
        .mapNotNull { it.firstOrNull() }
        .take(2)
        .joinToString("")
        .uppercase()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(PrimaryBlue, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.headlineLarge,
                color = White,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = fullName,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun ProfileInfoCard(
    email: String,
    city: String?,
    birthday: String?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ProfileInfoRow(
                iconRes = R.drawable.ic_email,
                label = "Email",
                value = email,
            )

            if (city != null) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                )
                ProfileInfoRow(
                    iconRes = R.drawable.ic_location,
                    label = stringResource(R.string.city),
                    value = city,
                )
            }

            if (birthday != null) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                )
                ProfileInfoRow(
                    iconRes = R.drawable.ic_calendar,
                    label = stringResource(R.string.birth_date),
                    value = formatBirthday(birthday),
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    iconRes: Int,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = SecondaryText,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = SecondaryText,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ProfileActionItem(
    iconRes: Int,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
            )
        }
    }
}

private fun formatBirthday(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy 'г.'", Locale("ru"))
        date.format(formatter)
    } catch (_: Exception) {
        dateString
    }
}

// region Previews

@Preview(showBackground = true, name = "Профиль — контент")
@Composable
private fun ProfileContentPreview() {
    GoogleClassTheme {
        ProfileScreenContent(
            state = ProfileScreenState.Content(
                firstName = "Алексей",
                lastName = "Сидоров",
                email = "sidorov@student.ru",
                city = "Москва",
                birthday = "2003-03-10",
            ),
            onNavigateBack = {},
            onArchivedCoursesClick = {},
            onLogoutClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Профиль — загрузка")
@Composable
private fun ProfileLoadingPreview() {
    GoogleClassTheme {
        ProfileScreenContent(
            state = ProfileScreenState.Loading,
            onNavigateBack = {},
            onArchivedCoursesClick = {},
            onLogoutClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Профиль — ошибка")
@Composable
private fun ProfileErrorPreview() {
    GoogleClassTheme {
        ProfileScreenContent(
            state = ProfileScreenState.Error("Не удалось загрузить профиль"),
            onNavigateBack = {},
            onArchivedCoursesClick = {},
            onLogoutClick = {},
        )
    }
}

// endregion
