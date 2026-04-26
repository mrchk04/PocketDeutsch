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
import kotlinx.serialization.Serializable
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
    
    IMPORTANT LANGUAGE RULE: First, detect the language of the student's text. If the text is primarily written in Ukrainian, English, or any language other than German, YOU MUST ABORT and return exactly this JSON:
    {
      "score": 0, "grammarScore": 0, "vocabularyScore": 0, "contentScore": 0,
      "overallFeedback": "Текст написано не німецькою мовою. Будь ласка, напиши свою відповідь німецькою, щоб я міг її перевірити.",
      "checklistEvaluations": [], "textCorrections": []
    }

    If the text IS in German, evaluate the student's text for BOTH strict grammatical accuracy AND stylistic naturalness (e.g., politeness, appropriate tone, using 'möchte' instead of 'will' where fitting). 
    Provide detailed scores (0-100) for grammarScore, vocabularyScore, and contentScore. The overall 'score' MUST be the average of these three.
    
    Do not use markdown blocks like ```json, just output the raw JSON.
    {
      "score": <Int between 0 and 100>,
      "grammarScore": <Int between 0 and 100>,
      "vocabularyScore": <Int between 0 and 100>,
      "contentScore": <Int between 0 and 100>,
      "overallFeedback": "<String. Friendly feedback in Ukrainian>",
      "checklistEvaluations": [
        { "requirementId": "<String>", "isFulfilled": <Boolean>, "comment": "<String in Ukrainian>" }
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

    private val grammarModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        },
        systemInstruction = content {
            text(
                """
                You are a strict, direct, and technical German language validator. 
                Do NOT use flattering, supportive, pedagogical, or childish language. Your feedback must be dry and analytical.
                
                CRITICAL RULES:
                1. PRIORITY: Grammatical and stylistic accuracy is the ONLY criteria for the "isCorrect" flag. 
                2. IGNORE LENGTH: If the instruction asks for 4-5 sentences, but the student writes only 1 sentence, DO NOT mark it as incorrect. Evaluate whatever text is provided solely for grammar.
                3. Explain ONLY the exact mistake the student made. 
                4. Do NOT discuss or correct parts of the text that are already correct. 
                5. Be absolutely precise with grammatical cases (Nominativ, Akkusativ, Dativ, Genitiv). Do not invent rules.
                
                Check the student's short text for grammatical and stylistic errors based on the instruction.
                Output the raw JSON EXACTLY in this format without markdown:
                {
                  "isCorrect": <Boolean. true if there are absolutely no grammatical/stylistic errors in the provided text, false otherwise>,
                  "feedback": "<String in Ukrainian. MUST start with 'Правильно: ' or 'Неправильно: '. Follow immediately with a short, factual explanation of the specific error. If the text is grammatically correct but too short, start with 'Правильно: ' and you MAY add a dry note at the end (e.g., 'Граматично все вірно. Зверніть увагу: за умовами завдання очікувався довший текст.'). Max 1-2 sentences.>",
                  "correctedText": "<String. The fully corrected version of the text in German.>"
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

    suspend fun checkSimpleGrammar(instruction: String, studentText: String): SimpleGrammarCheckResult {
        val prompt = """
            Task Instruction: "$instruction"
            
            Student's Text:
            "$studentText"
        """.trimIndent()

        val response = grammarModel.generateContent(prompt)
        val responseText = response.text ?: throw Exception("Отримано порожню відповідь від ШІ")

        return jsonParser.decodeFromString<SimpleGrammarCheckResult>(responseText)
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

@Serializable
data class SimpleGrammarCheckResult(
    val isCorrect: Boolean,
    val feedback: String,
    val correctedText: String
)