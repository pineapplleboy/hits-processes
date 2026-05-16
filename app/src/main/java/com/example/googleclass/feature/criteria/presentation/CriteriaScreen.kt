package com.example.googleclass.feature.criteria.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.googleclass.R
import com.example.googleclass.common.presentation.component.LoadingState
import com.example.googleclass.common.presentation.theme.GoogleClassTheme
import com.example.googleclass.common.presentation.theme.Outline
import com.example.googleclass.common.presentation.theme.PrimaryBlue
import com.example.googleclass.common.presentation.theme.SecondaryText
import com.example.googleclass.feature.criteria.domain.model.CriterionGrading
import com.example.googleclass.feature.criteria.domain.model.EvaluationCriterion
import org.koin.androidx.compose.koinViewModel

@Composable
fun CriteriaScreen(
    onNavigateBack: () -> Unit,
) {
    val viewModel: CriteriaViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uiEffect by viewModel.uiEffect.collectAsStateWithLifecycle(CriteriaUiEffect.None)
    val context = LocalContext.current

    LaunchedEffect(uiEffect) {
        when (val effect = uiEffect) {
            CriteriaUiEffect.NavigateBack -> {
                viewModel.consumeEffect()
                onNavigateBack()
            }
            CriteriaUiEffect.None -> Unit
            is CriteriaUiEffect.ShowMessage -> {
                Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                viewModel.consumeEffect()
            }
        }
    }

    CriteriaContent(
        state = uiState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun CriteriaContent(
    state: CriteriaUiState,
    onEvent: (CriteriaUiEvent) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CriteriaHeader(
                count = (state as? CriteriaUiState.Content)?.criteria?.size ?: 0,
                isSaving = (state as? CriteriaUiState.Content)?.isSaving == true,
                onBackClick = { onEvent(CriteriaUiEvent.NavigateBack) },
                onSaveClick = { onEvent(CriteriaUiEvent.Save) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(CriteriaUiEvent.AddCriterion) },
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.navigationBarsPadding(),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.criteria_add),
                )
            }
        },
    ) { padding ->
        when (state) {
            CriteriaUiState.Loading -> LoadingState()
            is CriteriaUiState.Content -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    itemsIndexed(
                        items = state.criteria,
                        key = { _, criterion -> criterion.id },
                    ) { index, criterion ->
                        CriterionCard(
                            index = index + 1,
                            criterion = criterion,
                            onEditClick = {
                                onEvent(CriteriaUiEvent.EditCriterion(criterion.id))
                            },
                            onDeleteClick = {
                                onEvent(CriteriaUiEvent.DeleteCriterion(criterion.id))
                            },
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CriteriaHeader(
    count: Int,
    isSaving: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    Surface(
        color = PrimaryBlue,
        contentColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                    )
                }

                TextButton(
                    onClick = onSaveClick,
                    enabled = !isSaving,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = stringResource(R.string.save),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.criteria_screen_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Text(
                text = stringResource(R.string.criteria_count_format, count),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.92f),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun CriterionCard(
    index: Int,
    criterion: EvaluationCriterion,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = index.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = criterion.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                TextButton(
                    onClick = onEditClick,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                ) {
                    Text(
                        text = stringResource(R.string.criteria_edit),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            CriterionMeta(criterion = criterion)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = stringResource(R.string.criteria_delete),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun CriterionMeta(
    criterion: EvaluationCriterion,
) {
    when (val grading = criterion.grading) {
        CriterionGrading.PassFail -> {
            MetaChip(
                text = stringResource(R.string.criteria_pass_fail),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface,
            )
        }

        is CriterionGrading.Range -> {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MetaChip(
                        text = "${grading.minValue}-${grading.maxValue}",
                        backgroundColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.surface,
                    )

                    grading.multiplier?.let { multiplier ->
                        OutlineChip(text = "x$multiplier")
                    }
                }

                grading.maxPoints?.let { maxPoints ->
                    Text(
                        text = stringResource(R.string.criteria_max_points_format, maxPoints),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText,
                    )
                }
            }
        }
    }
}

@Composable
private fun MetaChip(
    text: String,
    backgroundColor: Color,
    contentColor: Color,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun OutlineChip(
    text: String,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Outline, RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CriteriaContentPreview() {
    GoogleClassTheme {
        CriteriaContent(
            state = CriteriaUiState.Content(
                criteria = listOf(
                    EvaluationCriterion(
                        id = "1",
                        title = "Код компилируется",
                        grading = CriterionGrading.PassFail,
                    ),
                    EvaluationCriterion(
                        id = "2",
                        title = "Реализованы все классы",
                        grading = CriterionGrading.PassFail,
                    ),
                    EvaluationCriterion(
                        id = "3",
                        title = "Использованы принципы ООП",
                        grading = CriterionGrading.Range(
                            minValue = 0,
                            maxValue = 40,
                            multiplier = 0.4f,
                            maxPoints = 40,
                        ),
                    ),
                    EvaluationCriterion(
                        id = "4",
                        title = "Тесты написаны",
                        grading = CriterionGrading.PassFail,
                    ),
                ),
                isSaving = false,
            ),
            onEvent = {},
        )
    }
}
