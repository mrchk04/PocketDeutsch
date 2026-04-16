package com.mrchk.pocketdeutsch.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.mrchk.pocketdeutsch.BuildConfig
import kotlinx.serialization.json.Json
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.mrchk.pocketdeutsch.data.local.WrittenTaskDao
import com.mrchk.pocketdeutsch.data.local.WrittenTaskResultEntity
import com.mrchk.pocketdeutsch.domain.model.AiEvaluationResult
import com.mrchk.pocketdeutsch.domain.model.WritingTask
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GeminiRepository @Inject constructor(
    private val writtenTaskDao: WrittenTaskDao,
) {

    private val jsonParser = Json { ignoreUnknownKeys = true }

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash-lite",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        },
        systemInstruction = content {
            text(
                """
                You are an expert German language tutor. 
                
                IMPORTANT LANGUAGE RULE: 
                First, detect the language of the student's text. If the text is primarily written in Ukrainian, English, or any language other than German (minor borrowings or names are allowed, but the core text MUST be German), YOU MUST ABORT the evaluation and return exactly this JSON:
                {
                  "score": 0,
                  "grammarScore": 0,
                  "vocabularyScore": 0,
                  "contentScore": 0,
                  "overallFeedback": "Текст написано не німецькою мовою. Будь ласка, напиши свою відповідь німецькою, щоб я міг її перевірити.",
                  "checklistEvaluations": [],
                  "textCorrections": []
                }
                
                If the text IS in German, evaluate the student's text for BOTH strict grammatical accuracy AND stylistic naturalness (e.g., politeness, appropriate tone, using 'möchte' instead of 'will' where fitting). 
                
                Provide detailed scores (0-100) for Grammar, Vocabulary, and Content. The overall 'score' should be the average of these three.
                
                Do not use markdown blocks like ```json, just output the raw JSON strictly matching this structure:
                {
                  "score": <Int between 0 and 100>,
                  "grammarScore": <Int between 0 and 100. Evaluate sentence structure, cases, conjugations, syntax>,
                  "vocabularyScore": <Int between 0 and 100. Evaluate word choice, spelling, and variety>,
                  "contentScore": <Int between 0 and 100. Evaluate how well the text addresses the task requirements>,
                  "overallFeedback": "<String. Friendly and encouraging feedback in Ukrainian summarizing the result>",
                  "checklistEvaluations": [
                    { "requirementId": "<String. ID of the requirement from the prompt>", "isFulfilled": <Boolean>, "comment": "<String in Ukrainian>" }
                  ],
                  "textCorrections": [
                    { 
                      "originalIncorrectText": "<String. ONLY the specific word or short phrase with the error. DO NOT include the entire sentence. Keep it as minimal as possible>", 
                      "suggestedCorrection": "<String. Only the corrected word/phrase>", 
                      "explanation": "<String in Ukrainian. Explain clearly if it's a strict grammar mistake OR a stylistic/politeness improvement>", 
                      "startIndex": <Int>, 
                      "endIndex": <Int> 
                    }
                  ]
                }
                """.trimIndent()
            )
        }
    )

    suspend fun evaluateText(task: WritingTask, studentText: String): AiEvaluationResult {
        val checklistText = task.requiredPoints.joinToString("\n") {
            "- ID: ${it.id}, Requirement: ${it.text}"
        }

        val prompt = """
            Evaluate the following German text based on this task:
            Task: "${task.promptText}"
            
            Checklist of requirements to fulfill:
            $checklistText
            
            Student's Text:
            "$studentText"
        """.trimIndent()

        val response = generativeModel.generateContent(prompt)
        val responseText = response.text ?: throw Exception("Отримано порожню відповідь від ШІ")

        return jsonParser.decodeFromString<AiEvaluationResult>(responseText)
    }

    suspend fun saveResult(
        exerciseId: String,
        originalText: String,
        evaluation: AiEvaluationResult,
    ) {
        val entity = WrittenTaskResultEntity(
            exerciseId = exerciseId,
            originalText = originalText,
            overallScore = evaluation.score,
            grammarScore = evaluation.grammarScore,
            vocabularyScore = evaluation.vocabularyScore,
            contentScore = evaluation.contentScore,
            overallFeedback = evaluation.overallFeedback,
            corrections = evaluation.textCorrections,
            checklistEvaluations = evaluation.checklistEvaluations,
            pendingSync = true,
        )
        writtenTaskDao.insertTaskResult(entity)
    }

    fun getHistoryForExercise(exerciseId: String): Flow<List<WrittenTaskResultEntity>> {
        return writtenTaskDao.getResultsForExercise(exerciseId)
    }
}