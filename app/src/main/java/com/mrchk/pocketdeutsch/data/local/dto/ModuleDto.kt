package com.mrchk.pocketdeutsch.data.local.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModuleResponse(
    val module: ModuleData
)

@Serializable
data class ModuleData(
    val metadata: Metadata,
    @SerialName("block1_vocabulary") val block1Vocabulary: Block1Vocabulary,
    @SerialName("block2_grammar") val block2Grammar: Block2Grammar,
    @SerialName("block3_skills") val block3Skills: Block3Skills
)

// --- METADATA ---

@Serializable
data class Metadata(
    val id: String,
    val topic: String,
    @SerialName("topic_de") val topicDe: String,
    val level: String,
    @SerialName("grammar_topic") val grammarTopic: String,
    @SerialName("grammar_topic_connection") val grammarTopicConnection: String,
    @SerialName("exam_formats") val examFormats: List<String>,
    @SerialName("cefr_skills_covered") val cefrSkillsCovered: List<String>,
    @SerialName("estimated_minutes") val estimatedMinutes: Int,
    @SerialName("pronunciation_note") val pronunciationNote: String
)

// --- BLOCK 1: VOCABULARY ---

@Serializable
data class Block1Vocabulary(
    @SerialName("vocabulary_items") val vocabularyItems: List<VocabularyItem>,
    val collocations: List<Collocation>,
    @SerialName("words_in_context") val wordsInContext: List<WordInContext>,
    val exercises: List<StandardExercise>
)

@Serializable
data class VocabularyItem(
    val word: String,
    val article: String?,
    val plural: String?,
    @SerialName("word_class") val wordClass: String,
    val english: String,
    @SerialName("example_sentence") val exampleSentence: String,
    val register: String
)

@Serializable
data class Collocation(
    val phrase: String,
    val translation: String,
    val example: String,
    val register: String? = null
)

@Serializable
data class WordInContext(
    val word: String,
    val sentences: List<String>
)

// Універсальний клас для більшості вправ з масивом items та answers
@Serializable
data class StandardExercise(
    val type: String,
    val instruction: String,
    val items: List<String>,
    val answers: List<String>
)

// --- BLOCK 2: GRAMMAR ---

@Serializable
data class Block2Grammar(
    @SerialName("grammar_topic") val grammarTopic: String,
    val explanation: String,
    val rules: List<GrammarRule>,
    @SerialName("forms_table") val formsTable: FormsTable? = null,
    val achtung: List<String>,
    @SerialName("topic_connection_examples") val topicConnectionExamples: List<String>,
    val exercises: List<StandardExercise>
)

@Serializable
data class GrammarRule(
    val rule: String,
    val example: String
)

@Serializable
data class FormsTable(
    val columns: List<String>,
    val rows: List<List<String>>
)

// --- BLOCK 3: SKILLS ---

@Serializable
data class Block3Skills(
    val reading: ReadingSkill,
    val listening: ListeningSkill,
    @SerialName("language_use") val languageUse: List<LanguageUseTask>,
    val writing: WritingSkill,
    val speaking: SpeakingSkill
)

@Serializable
data class ReadingSkill(
    @SerialName("text_type") val textType: String,
    val text: String,
    @SerialName("exercise_type") val exerciseType: String,
    val instruction: String,
    val items: List<String>,
    val answers: List<String>
)

@Serializable
data class ListeningSkill(
    @SerialName("audio_type") val audioType: String,
    val transcript: String,
    @SerialName("tts_instructions") val ttsInstructions: String,
    @SerialName("exercise_type") val exerciseType: String,
    val instruction: String,
    val items: List<String>,
    val answers: List<String>
)

@Serializable
data class LanguageUseTask(
    val subtype: String,
    val instruction: String,
    @SerialName("text_with_gaps") val textWithGaps: String,
    val items: List<LanguageUseItem>
)

@Serializable
data class LanguageUseItem(
    @SerialName("gap_number") val gapNumber: Int,
    val options: List<String>,
    val answer: String,
    val explanation: String
)

@Serializable
data class WritingSkill(
    @SerialName("task_type") val taskType: String,
    val situation: String,
    val recipient: String,
    val register: String,
    @SerialName("required_points") val requiredPoints: List<String>,
    @SerialName("word_count_target") val wordCountTarget: Int,
    @SerialName("useful_phrases") val usefulPhrases: List<String>,
    @SerialName("model_answer") val modelAnswer: String,
    @SerialName("scoring_criteria") val scoringCriteria: List<String>
)

@Serializable
data class SpeakingSkill(
    @SerialName("task_type") val taskType: String,
    val prompt: String,
    @SerialName("image_description") val imageDescription: String,
    @SerialName("time_suggestion_seconds") val timeSuggestionSeconds: Int,
    @SerialName("useful_phrases") val usefulPhrases: List<String>,
    @SerialName("example_response") val exampleResponse: String,
    @SerialName("exam_tips") val examTips: List<String>
)