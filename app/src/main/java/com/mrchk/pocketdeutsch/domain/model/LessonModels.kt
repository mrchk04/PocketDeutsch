package com.mrchk.pocketdeutsch.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Lesson(
    val lessonId: String,
    val title: String,
    val level: String,
    val topic: String,
    val theory: Theory,
    val schriftlicherAusdruck: SchriftlicherAusdruck
    // Інші поля (wortschatz, leseverstehen тощо) ми додамо пізніше,
    // коли дійдемо до їхніх екранів.
)

@Serializable
data class Theory(
    val text: String,
    val grammar: Grammar
)

@Serializable
data class Grammar(
    val topic: String,
    val explanation: String,
    val keyForms: List<String>
)

@Serializable
data class SchriftlicherAusdruck(
    val format: String,
    val type: String,
    val instruction: String,
    val points: Int,
    val modelAnswer: String? = null
)