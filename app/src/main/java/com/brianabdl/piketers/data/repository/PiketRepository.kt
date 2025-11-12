package com.brianabdl.piketers.data.repository

import com.brianabdl.piketers.data.models.DayOfWeek
import com.brianabdl.piketers.data.models.PiketAssignment
import com.brianabdl.piketers.data.models.PiketSchedule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

class PiketRepository(
    private var members: List<String> = emptyList()
) {

    // Schedule definition
    private val schedule = mapOf(
        "Senin" to PiketSchedule("Senin", "jendela", listOf(16)),
        "Selasa" to PiketSchedule("Selasa", "makan", listOf(3, 17)),
        "Rabu" to PiketSchedule("Rabu", "sampah", listOf(17)),
        "Kamis" to PiketSchedule("Kamis", "lantai 1", listOf(16)),
        "Jumat" to PiketSchedule("Jumat", "lantai 2", listOf(16)),
        "Sabtu" to PiketSchedule("Sabtu", "lantai 3", listOf(16)),
        "Minggu" to PiketSchedule("Minggu", "tangga", listOf(16))
    )

    // Current assignment
    private val _currentAssignment = MutableStateFlow<PiketAssignment?>(null)
    val currentAssignment: StateFlow<PiketAssignment?> = _currentAssignment.asStateFlow()

    // Update members list
    fun updateMembers(newMembers: List<String>) {
        members = newMembers
    }

    // Get current day name in Indonesian
    fun getCurrentDayName(): String {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        // Convert Java Calendar day (1=Sunday, 2=Monday, etc.) to our enum
        return when (dayOfWeek) {
            Calendar.SUNDAY -> DayOfWeek.SUNDAY.indonesianName
            Calendar.MONDAY -> DayOfWeek.MONDAY.indonesianName
            Calendar.TUESDAY -> DayOfWeek.TUESDAY.indonesianName
            Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY.indonesianName
            Calendar.THURSDAY -> DayOfWeek.THURSDAY.indonesianName
            Calendar.FRIDAY -> DayOfWeek.FRIDAY.indonesianName
            Calendar.SATURDAY -> DayOfWeek.SATURDAY.indonesianName
            else -> DayOfWeek.SUNDAY.indonesianName
        }
    }

    // Randomly shuffle members
    private fun randomizeMemberList(): List<String> {
        return members.shuffled()
    }

    // Generate message based on task type and assignments
    fun generateMessage(taskType: String, shuffledMembers: List<String>): String {
        val message = StringBuilder("*JADWAL PIKET ${taskType.uppercase()}*\n\n")
        try {
            when (taskType) {
                "jendela" -> {
                    val chunk = shuffledMembers.chunked(2)
                    if (chunk.isNotEmpty()) {
                        message.appendLine("Lantai 1")
                        message.appendLine("Dalam: ${chunk[0].getOrElse(0, { "-" })}")
                        message.appendLine("Luar: ${chunk[0].getOrElse(1, { "-" })}")
                        message.appendLine()
                    }
                    if (chunk.size > 1) {
                        message.appendLine("Lantai 2")
                        message.appendLine("Dalam: ${chunk[1].getOrElse(0, { "-" })}")
                        message.appendLine("Luar: ${chunk[1].getOrElse(1, { "-" })}")
                        message.appendLine()
                    }
                    if (chunk.size > 2) {
                        message.appendLine("Lantai 3")
                        message.appendLine("Dalam: ${chunk[2].getOrElse(0, { "-" })}")
                        message.appendLine("Luar: ${chunk[2].getOrElse(1, { "-" })}")
                        message.appendLine()
                    }
                    if (chunk.size > 3) {
                        message.appendLine("Sekat Besar: ${chunk[3].getOrElse(0, { "-" })}")
                        message.appendLine()
                    }
                    message.appendLine("Monggo setelah piket bisa di react jika sudah selesai piket.")
                    message.append("Alhamdulillah Jazakumullahukhoiro.")
                }

                "tangga" -> {
                    val chunk = shuffledMembers.chunked(2)
                    if (chunk.isNotEmpty()) {
                        message.appendLine("Laki-Laki")
                        message.appendLine("Lantai 1 - 2: ${chunk[0].getOrElse(0, { "-" })}")
                        message.appendLine("Lantai 2 - 3: ${chunk[0].getOrElse(1, { "-" })}")
                        message.appendLine()
                    }
                    if (chunk.size > 1) {
                        message.appendLine("Perempuan")
                        message.appendLine("Lantai 1 - 2: ${chunk[1].getOrElse(0, { "-" })}")
                        message.appendLine("Lantai 2 - 3: ${chunk[1].getOrElse(1, { "-" })}")
                        message.appendLine()
                    }
                    if (chunk.size > 2) {
                        message.appendLine("Belakang")
                        message.appendLine("Lantai 1 - 2: ${chunk[2].getOrElse(0, { "-" })}")
                        message.appendLine("Lantai 2 - 3: ${chunk[2].getOrElse(1, { "-" })}")
                        message.appendLine()
                    }
                    if (chunk.size > 3) {
                        message.appendLine("Sekat Besar: ${chunk[3].getOrElse(0, { "-" })}")
                        message.appendLine()
                    }
                    message.appendLine("Monggo setelah piket bisa di react jika sudah selesai piket.")
                    message.append("Alhamdulillah Jazakumullahukhoiro.")
                }

                "makan" -> {
                    val half = shuffledMembers.size / 2
                    val pagiMembers = shuffledMembers.take(half)
                    val soreMembers = shuffledMembers.drop(half)

                    message.appendLine("=== Pagi ===")
                    pagiMembers.forEachIndexed { index, name ->
                        message.appendLine("${index + 1}. $name")
                    }

                    message.appendLine()
                    message.appendLine("=== Sore ===")
                    soreMembers.forEachIndexed { index, name ->
                        message.appendLine("${index + 1}. $name")
                    }
                }

                "sampah", "lantai 1", "lantai 2", "lantai 3" -> {
                    return "Kuy, piket $taskType"
                }
            }
        } catch (e: Exception) {
            message.appendLine("Terjadi kesalahan saat menghasilkan pesan.")
            e.printStackTrace()
        }

        return message.toString()
    }

    // Generate today's piket assignment
    fun generateTodayPiketAssignment() {
        val currentDay = getCurrentDayName()
        val todaySchedule = schedule[currentDay]

        todaySchedule?.let {
            val shuffledMembers = randomizeMemberList()
            val message = generateMessage(it.taskType, shuffledMembers)

            _currentAssignment.value = PiketAssignment(
                taskType = it.taskType,
                assignments = shuffledMembers,
                message = message
            )
        }
    }

    // Get all members
    fun getAllMembers(): List<String> = members

    // Get all schedule
    fun getAllSchedule(): Map<String, PiketSchedule> = schedule
}