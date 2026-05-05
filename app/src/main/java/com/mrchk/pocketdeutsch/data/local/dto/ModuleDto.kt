package com.mrchk.pocketdeutsch.data.local.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ModuleResponse(
    val module: ModuleData,
)

@Serializable
data class ModuleData(
    val metadata: Metadata = Metadata(),
    @SerialName("block1_vocabulary") val block1Vocabulary: Block1Vocabulary = Block1Vocabulary(),
    @SerialName("block2_grammar") val block2Grammar: Block2Grammar = Block2Grammar(),
    @SerialName("block3_skills") val block3Skills: Block3Skills = Block3Skills(),
)

@Serializable
data class Metadata(
    val id: String = "",
    val topic: String = "",
    @SerialName("topic_de") val topicDe: String = "",
    val level: String = "",
    @SerialName("grammar_topic") val grammarTopic: String = "",
    @SerialName("grammar_topic_connection") val grammarTopicConnection: String = "",
    @SerialName("short_description") val shortDescription: String = "",
    @SerialName("detailed_description") val detailedDescription: String = "",
    @SerialName("exam_formats") val examFormats: List<String> = emptyList(),
    @SerialName("cefr_skills_covered") val cefrSkillsCovered: List<String> = emptyList(),
    @SerialName("estimated_minutes") val estimatedMinutes: Int = 0,
    @SerialName("pronunciation_note") val pronunciationNote: String = "",
)

@Serializable
data class Block1Vocabulary(
    @SerialName("vocabulary_items") val vocabularyItems: List<VocabularyItem> = emptyList(),
    val collocations: List<Collocation> = emptyList(),
    @SerialName("words_in_context") val wordsInContext: List<WordInContext> = emptyList(),
    val exercises: List<StandardExercise> = emptyList(),
)

@Serializable
data class VocabularyItem(
    val word: String = "",
    val article: String? = null,
    val plural: String? = null,
    @SerialName("word_class") val wordClass: String = "",
    val ukrainian: String = "",
    @SerialName("example_sentence") val exampleSentence: String = "",
    val register: String = "",
)

@Serializable
data class Collocation(
    val phrase: String = "",
    val translation: String = "",
    val example: String = "",
    val register: String? = null,
)

@Serializable
data class WordInContext(
    val word: String = "",
    val sentences: List<String> = emptyList(),
)

@Serializable
data class StandardExercise(
    val type: String = "",
    val instruction: String = "",
    val items: List<String> = emptyList(),
    val words: List<String> = emptyList(),
    val definitions: List<String> = emptyList(),
    val answers: List<String> = emptyList(),
    val text: String? = null,
    val tip: String? = null,
    val verbs: List<String> = emptyList(),
    val nouns: List<String> = emptyList(),
    val options: List<String> = emptyList(),
)

@Serializable
data class Block2Grammar(
    @SerialName("grammar_topic") val grammarTopic: String = "",
    @SerialName("explanation_ua") val explanationUa: String = "",
    val rules: List<GrammarRule> = emptyList(),
    @SerialName("forms_table") val formsTable: FormsTable? = null,
    @SerialName("contractions_note") val contractionsNote: String? = null,
    @SerialName("wo_wohin_contrast") val woWohinContrast: WoWohinContrast? = null,
    val achtung: List<String> = emptyList(),
    @SerialName("topic_connection_examples") val topicConnectionExamples: List<String> = emptyList(),
    val exercises: List<StandardExercise> = emptyList(),
)

@Serializable
data class GrammarRule(
    val rule: String = "",
    val example: String = "",
)

@Serializable
data class WoWohinContrast(
    val title: String = "",
    val rows: List<WoWohinRow> = emptyList(),
)

@Serializable
data class WoWohinRow(
    val verb: String = "",
    val question: String = "",
    val case: String = "",
    val example: String = "",
)

@Serializable
data class FormsTable(
    val title: String? = null,
    val columns: List<String> = emptyList(),
    val rows: List<List<String>> = emptyList(),
)

@Serializable
data class Block3Skills(
    val reading: ReadingSkillContainer = ReadingSkillContainer(),
    val listening: ListeningSkillContainer = ListeningSkillContainer(),
    @SerialName("language_use") val languageUse: List<LanguageUseTask> = emptyList(),
    val writing: WritingSkill = WritingSkill(),
    val speaking: SpeakingSkillContainer = SpeakingSkillContainer(),
)

@Serializable
data class ReadingSkillContainer(
    val exercise_1: ReadingExercise? = null,
    val exercise_2: ReadingExercise? = null,
)

@Serializable
data class ReadingExercise(
    @SerialName("text_type") val textType: String = "",
    val title: String = "",
    val instruction: String = "",
    val text: String? = null,
    val texts: Map<String, String>? = null,
    val items: List<String> = emptyList(),
    val answers: List<String> = emptyList(),
)

@Serializable
data class ListeningSkillContainer(
    val listening_1: ListeningExercise? = null,
    val listening_2: ListeningExercise? = null,
)

@Serializable
data class ListeningExercise(
    val title: String = "",
    @SerialName("audioUrl") val audioUrl: String? = null,
    @SerialName("audio_note") val audioNote: String = "",
    val transcript: String = "",
    @SerialName("exercise_type") val exerciseType: String = "",
    val instruction: String = "",
    val items: JsonElement? = null,
    val answers: List<String> = emptyList(),
)

@Serializable
data class McQuestionDto(
    val question: String = "",
    val options: List<String> = emptyList(),
    val answer: String = ""
)

@Serializable
data class LanguageUseTask(
    val subtype: String = "",
    val title: String = "",
    val instruction: String = "",
    @SerialName("text_with_gaps") val textWithGaps: String = "",
    val items: List<LanguageUseItem> = emptyList(),
)

@Serializable
data class LanguageUseItem(
    @SerialName("gap_number") val gapNumber: Int = 0,
    val options: List<String> = emptyList(),
    val answer: String = "",
    val explanation: String? = null,
)

@Serializable
data class WritingSkill(
    @SerialName("task_type") val taskType: String = "",
    val situation: String = "",
    val recipient: String = "",
    val register: String = "",
    @SerialName("required_points") val requiredPoints: List<String> = emptyList(),
    @SerialName("word_count_target") val wordCountTarget: Int = 0,
    @SerialName("useful_phrases") val usefulPhrases: List<String> = emptyList(),
    @SerialName("model_answer") val modelAnswer: String = "",
    @SerialName("scoring_criteria") val scoringCriteria: List<String> = emptyList(),
)

@Serializable
data class SpeakingSkillContainer(
    @SerialName("task_types") val taskTypes: List<SpeakingTask> = emptyList(),
)

@Serializable
data class SpeakingTask(
    val type: String = "",
    val title: String = "",
    val prompt: String = "",
    @SerialName("image_description_for_teacher") val imageDescriptionForTeacher: String? = null,
    @SerialName("image_search_suggestion") val imageSearchSuggestion: String? = null,
    @SerialName("required_points") val requiredPoints: List<String> = emptyList(),
    @SerialName("time_suggestion_seconds") val timeSuggestionSeconds: Int = 0,
    @SerialName("useful_phrases") val usefulPhrases: List<String> = emptyList(),
    @SerialName("example_response") val exampleResponse: String? = null,
    @SerialName("exam_tips") val examTips: List<String> = emptyList(),
)