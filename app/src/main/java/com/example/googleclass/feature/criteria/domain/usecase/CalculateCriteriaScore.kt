package com.example.googleclass.feature.criteria.domain.usecase

import com.example.googleclass.feature.post.data.model.TaskMarkEvaluationType

/**
 * Ввод по одному критерию для расчёта итоговой оценки.
 */
data class CriterionScoreInput(
    val rawInput: String,
    val minScore: Float,
    val maxScore: Float,
    val multiplier: Float?,
)

/**
 * Считает итоговую оценку по введённым значениям критериев.
 *
 * Возвращает null, если список пуст, какое-то значение не парсится или выходит за
 * пределы [minScore, maxScore] — это позволяет переиспользовать логику и для
 * валидации, и для отображения предварительного результата.
 *
 * Формула зависит от типа оценивания задания и совпадает с расчётом
 * преподавательской проверки по критериям.
 */
fun calculateCriteriaScore(
    inputs: List<CriterionScoreInput>,
    evaluationType: TaskMarkEvaluationType?,
    taskMaxScore: Float,
): Float? {
    if (inputs.isEmpty()) return null

    val parsedScores = inputs.map { input ->
        val score = input.rawInput.replace(',', '.').toFloatOrNull() ?: return null
        if (score < input.minScore || score > input.maxScore) return null
        score
    }

    val rawScore = when (evaluationType) {
        TaskMarkEvaluationType.MEAN_VALUE -> parsedScores.average().toFloat()

        TaskMarkEvaluationType.COEFFICIENTS_SUM -> inputs.indices.sumOf { index ->
            val multiplier = inputs[index].multiplier ?: 1f
            (parsedScores[index] * multiplier).toDouble()
        }.toFloat()

        TaskMarkEvaluationType.COEFFICIENTS_MEAN_VALUE -> {
            val weightedScores = inputs.mapIndexed { index, input ->
                parsedScores[index] * (input.multiplier ?: 1f)
            }
            val totalWeight = inputs.sumOf { (it.multiplier ?: 1f).toDouble() }.toFloat()
            if (totalWeight > 0f) {
                weightedScores.sum() / totalWeight
            } else {
                parsedScores.average().toFloat()
            }
        }

        else -> parsedScores.sum()
    }

    return rawScore.coerceIn(0f, taskMaxScore)
}
