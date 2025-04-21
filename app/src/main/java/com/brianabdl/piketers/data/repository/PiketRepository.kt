package com.brianabdl.piketers.data.repository

import com.brianabdl.piketers.data.models.DayOfWeek
import com.brianabdl.piketers.data.models.PiketAssignment
import com.brianabdl.piketers.data.models.PiketSchedule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

class PiketRepository {

    // Members list
    private val members = listOf(
        "Mas Brian",
        "Mas Pras",
        "Mas Jojo",
        "Mas Azka",
        "Mas Dito",
        "Mas Nizar",
        "Mas Fendi"
    )

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
    private fun generateMessage(taskType: String, shuffledMembers: List<String>): String {
        val message = StringBuilder("*WAKTUNYA JADWAL PIKET ${taskType.uppercase()}*\n\n")

        when (taskType) {
            "jendela" -> {
                message.append("Lantai 1\nDalam: ${shuffledMembers[0]}\nLuar: ${shuffledMembers[1]}\n\n")
                message.append("Lantai 2\nDalam: ${shuffledMembers[2]}\nLuar: ${shuffledMembers[3]}\n\n")
                message.append("Lantai 3\nDalam: ${shuffledMembers[4]}\nLuar: ${shuffledMembers[5]}\n\n")
                message.append("Sekat Besar: ${shuffledMembers[6]}\n\n")
                message.append("Monggo setelah piket bisa di react jika sudah selesai piket.\n")
                message.append("Alhamdulillah Jazakumullahukhoiro.")
            }
            "tangga" -> {
                message.append("Laki-Laki\nLantai 1 - 2: ${shuffledMembers[0]}\nLantai 2 - 3: ${shuffledMembers[1]}\n\n")
                message.append("Perempuan\nLantai 1 - 2: ${shuffledMembers[2]}\nLantai 2 - 3: ${shuffledMembers[3]}\n\n")
                message.append("Belakang\nLantai 1 - 2: ${shuffledMembers[4]}\nLantai 2 - 3: ${shuffledMembers[5]}\n\n")
                message.append("Libur: ${shuffledMembers[6]}\n\n")
                message.append("Monggo setelah piket bisa di react jika sudah selesai piket.\n")
                message.append("Alhamdulillah Jazakumullahukhoiro.")
            }
            "makan", "sampah", "lantai 1", "lantai 2", "lantai 3" -> {
                message.append("Kuy, piket $taskType")
            }
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