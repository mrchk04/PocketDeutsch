package com.mrchk.pocketdeutsch.domain.model

import android.accessibilityservice.GestureDescription

data class Lesson(
    val lessonId: String,
    val level: String,
    val title: String,
    val topic: String,
    val shortDescription: String,
    val estimatedMinutes: Int,

    val vocabulary: VocabularySection,
    val grammar: GrammarSection,
    val examPractice: ExamPracticeSection
){
    val totalTasks: Int
        get() {
            var count = 2
            count += 1
            count += 1
            count += 1
            count += 1
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
    val exercises: List<VocabExercise>
)

sealed interface VocabExercise {
    val instruction: String // Спільне поле для всіх вправ

    data class Matching(
        override val instruction: String,
        val items: List<String>,     // Наприклад: "1. die Kaution"
        val options: List<String>,   // Наприклад: "a) monatliche Zahlung..."
        val answers: List<String>    // Наприклад: "1-b"
    ) : VocabExercise

    data class GapFill(
        override val instruction: String,
        val items: List<String>,     // Речення з пропусками
        val answers: List<String>
    ) : VocabExercise

    data class WordFormation(
        override val instruction: String,
        val items: List<String>,     // Слова для злиття
        val answers: List<String>
    ) : VocabExercise
}

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
    val textType: String,
    val text: String,
    val adParts: List<AdPart>? = null,
    val exercises: List<InteractiveExercise>
)

data class AdPart(
    val letter: String,
    val content: String
)

data class ListeningPractice(
    val audioUrls: List<String?>,
    val transcripts: List<String>,
    val exercises: List<InteractiveExercise>
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
    val wordsTargetCount: Int,
    val instruction: List<String>,
    val modelAnswer: String? = null,
    val criteria: List<EvaluationCriterion>,
    val usefulPhrases: List<String>
)

data class EvaluationCriterion(
    val criterion: String,
    val description: String,
    val maxPoints: Int
)

data class SpeakingPractice(
    val taskType: String,
    val prompt: String,
    val imageDescription: String,
    val timeSuggestionSeconds: Int,
    val usefulPhrases: List<String>,
    val exampleResponse: String,
    val examTips: List<String>
)

sealed interface InteractiveExercise {
    val instruction: String

    data class MultipleChoice(
        override val instruction: String,
        val questions: List<McQuestion>
    ) : InteractiveExercise

    data class RichtigFalsch(
        override val instruction: String,
        val items: List<String>,
        val answers: List<String>
    ) : InteractiveExercise

    data class Standard(
        val type: String,
        override val instruction: String,
        val items: List<String>,
        val answers: List<String>
    ) : InteractiveExercise
}

data class McQuestion(
    val question: String,
    val options: List<String>,
    val answer: String
)