package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

// Test double for the dataSource
class FakeAndroidDataSource : ReminderDataSource {

    val reminders = ArrayList<ReminderDTO>()

    override suspend fun getReminders() : Result<List<ReminderDTO>> {
        return Result.Success(reminders)
    }

    override suspend fun saveReminder(reminder : ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id : String) : Result<ReminderDTO> {
        for (reminder in reminders){
            if (reminder.id == id)
                return Result.Success(reminder)
        }
        return Result.Error("cannot find a reminder with this id")
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }
}