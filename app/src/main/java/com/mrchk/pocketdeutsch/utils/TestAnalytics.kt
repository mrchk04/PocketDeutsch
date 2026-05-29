package com.mrchk.pocketdeutsch.utils

data class ExerciseReport(
    val exerciseName: String,
    val timeSpentSeconds: Int,
    val correctAnswers: Int,
    val totalQuestions: Int
)

object TestAnalytics {
    private val reports = mutableListOf<ExerciseReport>()

    fun addReport(report: ExerciseReport) {
        reports.removeAll { it.exerciseName == report.exerciseName }
        reports.add(report)
    }

    fun generateCsv(moduleName: String, userName: String): String {
        val sb = StringBuilder()
        sb.append("Ім'я,Модуль,Вправа,Час(сек),Правильні відповіді,Всього питань,Відсоток успішності\n")

        reports.forEach { report ->
            val percentage = if (report.totalQuestions > 0) {
                (report.correctAnswers.toFloat() / report.totalQuestions * 100).toInt()
            } else 0

            sb.append("$userName,$moduleName,${report.exerciseName},${report.timeSpentSeconds},${report.correctAnswers},${report.totalQuestions},$percentage%\n")
        }
        return sb.toString()
    }

    fun clear() {
        reports.clear()
    }
}