package com.mrchk.pocketdeutsch.data.local

import androidx.room.TypeConverter
import com.mrchk.pocketdeutsch.domain.model.ChecklistEvaluation
import com.mrchk.pocketdeutsch.domain.model.TextCorrection
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DatabaseConverters {

    @TypeConverter
    fun fromCorrectionsList(value: List<TextCorrection>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toCorrectionsList(value: String): List<TextCorrection> {
        return try {
            Json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromChecklist(value: List<ChecklistEvaluation>): String = Json.encodeToString(value)

    @TypeConverter
    fun toChecklist(value: String): List<ChecklistEvaluation> = try {
        Json.decodeFromString(value)
    } catch (e: Exception) { emptyList() }
}