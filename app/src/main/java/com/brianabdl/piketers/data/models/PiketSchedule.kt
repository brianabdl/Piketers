package com.brianabdl.piketers.data.models

data class PiketSchedule(
    val dayName: String,
    val taskType: String,
    val times: List<Int>
)

data class PiketAssignment(
    val taskType: String,
    val assignments: List<String>,
    val message: String
)

// Enum for Indonesian days of the week
enum class DayOfWeek(val indonesianName: String) {
    SUNDAY("Minggu"),
    MONDAY("Senin"),
    TUESDAY("Selasa"),
    WEDNESDAY("Rabu"),
    THURSDAY("Kamis"),
    FRIDAY("Jumat"),
    SATURDAY("Sabtu")
}