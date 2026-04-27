package com.mrchk.pocketdeutsch.data.mapper

import com.mrchk.pocketdeutsch.data.local.dto.ModuleData
import com.mrchk.pocketdeutsch.data.local.dto.StandardExercise
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
import com.mrchk.pocketdeutsch.domain.model.VocabularySection
import com.mrchk.pocketdeutsch.domain.model.Word

fun ModuleData.toDomainModel(): Lesson {
    return Lesson(
        lessonId = this.metadata.id,
        level = this.metadata.level,
        title = this.metadata.topicDe,
        description = this.metadata.grammarTopicConnection,
        estimatedMinutes = this.metadata.estimatedMinutes,

        vocabulary = VocabularySection(
            words = this.block1Vocabulary.vocabularyItems.map {
                Word(it.word, it.english, it.exampleSentence, it.article, it.plural)
            },
            collocations = this.block1Vocabulary.collocations.map {
                CollocationUi(it.phrase, it.translation, it.example)
            },
            contextSentences = this.block1Vocabulary.wordsInContext.flatMap { it.sentences },
            exercises = this.block1Vocabulary.exercises.map { it.toDomainExercise() }
        ),

        grammar = GrammarSection(
            topic = this.block2Grammar.grammarTopic,
            explanation = this.block2Grammar.explanation,
            // Мапимо список правил у доменні моделі
            rules = this.block2Grammar.rules.map { dtoRule ->
                GrammarRuleDomain(
                    rule = dtoRule.rule,
                    example = dtoRule.example
                )
            },
            // Безпечно мапимо таблицю, якщо вона є
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
            reading = ReadingPractice(
                text = this.block3Skills.reading.text,
                exercise = InteractiveExercise(
                    type = this.block3Skills.reading.exerciseType,
                    instruction = this.block3Skills.reading.instruction,
                    items = this.block3Skills.reading.items,
                    answers = this.block3Skills.reading.answers
                )
            ),
            listening = ListeningPractice(
                transcript = this.block3Skills.listening.transcript,
                exercise = InteractiveExercise(
                    type = this.block3Skills.listening.exerciseType,
                    instruction = this.block3Skills.listening.instruction,
                    items = this.block3Skills.listening.items,
                    answers = this.block3Skills.listening.answers
                )
            ),
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
                            explanation = item.explanation
                        )
                    }
                )
            },
            writing = WritingExercise(
                format = this.block3Skills.writing.taskType,
                instruction = listOf(this.block3Skills.writing.situation) + this.block3Skills.writing.requiredPoints,
                modelAnswer = this.block3Skills.writing.modelAnswer,
                criteria = this.block3Skills.writing.scoringCriteria.map {
                    EvaluationCriterion("Kriterium", it, 5)
                }
            ),
            speaking = SpeakingPractice(
                taskType = this.block3Skills.speaking.taskType,
                prompt = this.block3Skills.speaking.prompt,
                imageDescription = this.block3Skills.speaking.imageDescription,
                timeSuggestionSeconds = this.block3Skills.speaking.timeSuggestionSeconds,
                usefulPhrases = this.block3Skills.speaking.usefulPhrases,
                exampleResponse = this.block3Skills.speaking.exampleResponse,
                examTips = this.block3Skills.speaking.examTips
            )
        )
    )
}

// Допоміжна функція для мапінгу вправ
fun StandardExercise.toDomainExercise(): InteractiveExercise = InteractiveExercise(
    type = this.type,
    instruction = this.instruction,
    items = this.items,
    answers = this.answers
)