package com.mrchk.pocketdeutsch.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.mrchk.pocketdeutsch.BuildConfig
import kotlinx.serialization.json.Json
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.mrchk.pocketdeutsch.domain.model.AiEvaluationResult
import com.mrchk.pocketdeutsch.domain.model.WritingTask
import javax.inject.Inject

class GeminiRepository @Inject constructor() {

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
                Evaluate the student's text for BOTH strict grammatical accuracy AND stylistic naturalness (e.g., politeness, appropriate tone, using 'möchte' instead of 'will' where fitting). 
                Do not use markdown blocks like ```json, just output the raw JSON.
                {
                  "score": <Int between 0 and 100>,
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
}