package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(val reminders : MutableList<ReminderDTO> = mutableListOf()) : ReminderDataSource {

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError || reminders.isEmpty())
            return Result.Error("there is no active reminders")
        return Result.Success(ArrayList(reminders))
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {

        if(shouldReturnError)
            return Result.Error("error loading reminders")

        for (reminder in reminders){
            if (reminder.id == id)
                return Result.Success(reminder)
        }
        return Result.Error("there is no reminder with this id")
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }

    private var shouldReturnError = false
    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }
}