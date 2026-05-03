package com.mrchk.pocketdeutsch.data.mapper

import android.util.Log
import com.mrchk.pocketdeutsch.data.local.dto.ModuleData
import com.mrchk.pocketdeutsch.data.local.dto.StandardExercise
import com.mrchk.pocketdeutsch.domain.model.AdPart
import com.mrchk.pocketdeutsch.domain.model.CollocationUi
import com.mrchk.pocketdeutsch.domain.model.Lesson
import com.mrchk.pocketdeutsch.domain.model.WritingExercise
import com.mrchk.pocketdeutsch.domain.model.EvaluationCriterion
import com.mrchk.pocketdeutsch.domain.model.ExamPracticeSection
import com.mrchk.pocketdeutsch.domain.model.FormsTableDomain
import com.mrchk.pocketdeutsch.domain.model.GapOption
import com.mrchk.pocketdeutsch.domain.model.GrammarRuleDomain
import com.mrchk.pocketdeutsch.domain.model.GrammarSection
import com.mrchk.pocketdeutsch.domain.model.InteractiveExercise
import com.mrchk.pocketdeutsch.domain.model.LanguageUsePractice
import com.mrchk.pocketdeutsch.domain.model.ListeningPractice
import com.mrchk.pocketdeutsch.domain.model.ReadingPractice
import com.mrchk.pocketdeutsch.domain.model.SpeakingPractice
import com.mrchk.pocketdeutsch.domain.model.VocabExercise
import com.mrchk.pocketdeutsch.domain.model.VocabularySection
import com.mrchk.pocketdeutsch.domain.model.Word

fun ModuleData.toDomainModel(): Lesson {
    return Lesson(
        lessonId = this.metadata.id,
        level = this.metadata.level,
        topic = this.metadata.topic,
        title = this.metadata.topicDe,
        shortDescription = this.metadata.shortDescription,
        estimatedMinutes = this.metadata.estimatedMinutes,

        vocabulary = VocabularySection(
            words = this.block1Vocabulary.vocabularyItems.map {
                Word(
                    it.word,
                    it.ukrainian ?: "ПОРОЖНЬО",
                    it.exampleSentence,
                    it.article,
                    it.plural
                )
            },
            collocations = this.block1Vocabulary.collocations.map {
                CollocationUi(it.phrase, it.translation, it.example)
            },
            contextSentences = this.block1Vocabulary.wordsInContext.flatMap { it.sentences },
            exercises = this.block1Vocabulary.exercises.mapNotNull { it.toVocabExercise() }
        ),

        grammar = GrammarSection(
            topic = this.block2Grammar.grammarTopic,
            explanation = this.block2Grammar.explanationUa,
            rules = this.block2Grammar.rules.map { dtoRule ->
                GrammarRuleDomain(
                    rule = dtoRule.rule,
                    example = dtoRule.example
                )
            },
            formsTable = this.block2Grammar.formsTable?.let { dtoTable ->
                FormsTableDomain(
                    columns = dtoTable.columns,
                    rows = dtoTable.rows
                )
            },
            warningNotes = this.block2Grammar.achtung,
            contextExamples = this.block2Grammar.topicConnectionExamples,
            exercises = this.block2Grammar.exercises.map { it.toDomainExercise() }
        ),

        examPractice = ExamPracticeSection(
            reading = run {
                val exercises = mutableListOf<InteractiveExercise>()
                val allAdParts = mutableListOf<AdPart>()
                var combinedText = ""

                this.block3Skills.reading.exercise_1?.let { ex1 ->
                    ex1.texts?.map { (letter, content) ->
                        allAdParts.add(AdPart(letter = letter, content = content))
                    }

                    combinedText = ex1.text ?: ex1.texts?.values?.joinToString("\n\n") ?: ""

                    exercises.add(
                        InteractiveExercise(
                            type = ex1.textType,
                            instruction = ex1.instruction,
                            items = ex1.items,
                            answers = ex1.answers
                        )
                    )
                }

                this.block3Skills.reading.exercise_2?.let { ex2 ->
                    if (combinedText.isEmpty()) combinedText = ex2.text ?: ""

                    exercises.add(
                        InteractiveExercise(
                            type = ex2.textType,
                            instruction = ex2.instruction,
                            items = ex2.items,
                            answers = ex2.answers
                        )
                    )
                }

                ReadingPractice(
                    textType = this.block3Skills.reading.exercise_1?.textType ?: "reading",
                    text = combinedText,
                    adParts = if (allAdParts.isNotEmpty()) allAdParts else null,
                    exercises = exercises
                )
            },

            listening = this.block3Skills.listening.listening_1?.let { listening1 ->
                ListeningPractice(
                    audioUrl = null,
                    transcript = listening1.transcript,
                    exercise = InteractiveExercise(
                        type = listening1.exerciseType,
                        instruction = listening1.instruction,
                        items = emptyList(),
                        answers = listening1.answers
                    )
                )
            } ?: ListeningPractice(null, "", InteractiveExercise("", "", emptyList(), emptyList())),

            languageUse = this.block3Skills.languageUse.map { task ->
                LanguageUsePractice(
                    subtype = task.subtype,
                    instruction = task.instruction,
                    textWithGaps = task.textWithGaps,
                    gaps = task.items.map { item ->
                        GapOption(
                            gapNumber = item.gapNumber,
                            options = item.options,
                            correctAnswer = item.answer,
                            explanation = item.explanation ?: ""
                        )
                    }
                )
            },
            writing = WritingExercise(
                format = this.block3Skills.writing.taskType,
                wordsTargetCount = this.block3Skills.writing.wordCountTarget,
                instruction = listOf(this.block3Skills.writing.situation) + this.block3Skills.writing.requiredPoints,
                modelAnswer = this.block3Skills.writing.modelAnswer,
                criteria = this.block3Skills.writing.scoringCriteria.map {
                    EvaluationCriterion("Kriterium", it, 5)
                },
                usefulPhrases = this.block3Skills.writing.usefulPhrases
            ),
            speaking = this.block3Skills.speaking.taskTypes.firstOrNull()?.let { firstTask ->
                SpeakingPractice(
                    taskType = firstTask.type,
                    prompt = firstTask.prompt,
                    imageDescription = firstTask.imageDescriptionForTeacher ?: "",
                    timeSuggestionSeconds = firstTask.timeSuggestionSeconds,
                    usefulPhrases = firstTask.usefulPhrases,
                    exampleResponse = firstTask.exampleResponse ?: "",
                    examTips = firstTask.examTips
                )
            } ?: SpeakingPractice("", "", "", 0, emptyList(), "", emptyList())
        )
    )
}

fun StandardExercise.toDomainExercise(): InteractiveExercise {
    val combinedItems = this.items.ifEmpty { this.words }
        .ifEmpty { this.options }
        .ifEmpty { this.verbs + this.nouns }

    return InteractiveExercise(
        type = this.type,
        instruction = this.instruction,
        items = combinedItems,
        answers = this.answers
    )
}

fun StandardExercise.toVocabExercise(): VocabExercise? {
    return when (this.type) {
        "matching" -> VocabExercise.Matching(
            instruction = this.instruction,
            items = this.items,
            options = this.options, // Беремо options прямо з твого DTO!
            answers = this.answers
        )
        "gap_fill" -> VocabExercise.GapFill(
            instruction = this.instruction,
            items = this.items,
            answers = this.answers
        )
//        "collocation_sort" -> VocabExercise.CollocationSort(
//            instruction = this.instruction,
//            verbs = this.verbs, // Беремо verbs з DTO
//            nouns = this.nouns, // Беремо nouns з DTO
//            answers = this.answers
//        )
        "word_formation" -> VocabExercise.WordFormation(
            instruction = this.instruction,
            items = this.items,
            answers = this.answers
        )
        else -> null // Ігноруємо sentence_production та інші невідомі
    }
}