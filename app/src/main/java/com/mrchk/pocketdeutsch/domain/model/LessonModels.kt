package com.mrchk.pocketdeutsch.domain.model

import com.mrchk.pocketdeutsch.data.local.dto.StandardExercise

data class Lesson(
    val lessonId: String,
    val level: String,
    val title: String,
    val description: String,
    val estimatedMinutes: Int,

    val vocabulary: VocabularySection,
    val grammar: GrammarSection,
    val examPractice: ExamPracticeSection
){
    val totalTasks: Int
        get() {
            var count = 2 // Лексика і Граматика є завжди

            // Якщо екзаменаційні блоки колись стануть nullable, тут можна буде додати перевірки (if != null).
            // Поки вони обов'язкові, рахуємо їх:
            count += 1 // Читання
            count += 1 // Аудіювання
            count += 1 // Письмо
            count += 1 // Говоріння

            // Мовні конструкції можуть бути порожнім списком, тому перевіряємо
            if (examPractice.languageUse.isNotEmpty()) {
                count += 1
            }

            return count
        }
}

data class VocabularySection(
    val words: List<Word>,
    val collocations: List<CollocationUi>,
    val contextSentences: List<String>,
    val exercises: List<InteractiveExercise>
)

data class Word(
    val word: String,
    val translation: String,
    val example: String,
    val article: String? = null,
    val plural: String? = null
)

data class CollocationUi(
    val phrase: String,
    val translation: String,
    val example: String
)

data class GrammarSection(
    val topic: String,
    val explanation: String,
    val rules: List<GrammarRuleDomain>,
    val formsTable: FormsTableDomain?,
    val warningNotes: List<String>,
    val contextExamples: List<String>,
    val exercises: List<InteractiveExercise>
)

data class GrammarRuleDomain(
    val rule: String,
    val example: String
)

data class FormsTableDomain(
    val columns: List<String>,
    val rows: List<List<String>>
)

data class ExamPracticeSection(
    val reading: ReadingPractice,
    val listening: ListeningPractice,
    val languageUse: List<LanguageUsePractice>,
    val writing: WritingExercise,
    val speaking: SpeakingPractice
)

data class ReadingPractice(
    val text: String,
    val exercise: InteractiveExercise
)

data class ListeningPractice(
    val transcript: String,
    val exercise: InteractiveExercise
)

data class LanguageUsePractice(
    val subtype: String,
    val instruction: String,
    val textWithGaps: String,
    val gaps: List<GapOption>
)

data class GapOption(
    val gapNumber: Int,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String
)

data class WritingExercise(
    val format: String,
    val instruction: List<String>,
    val modelAnswer: String? = null,
    val criteria: List<EvaluationCriterion>
)

data class EvaluationCriterion(
    val criterion: String,
    val description: String,
    val maxPoints: Int
)

data class SpeakingPractice(
    val taskType: String,
    val instruction: String,
    val usefulPhrases: List<String>,
    val examTips: List<String>
)

data class InteractiveExercise(
    val type: String,
    val instruction: String,
    val items: List<String>,
    val answers: List<String>
)