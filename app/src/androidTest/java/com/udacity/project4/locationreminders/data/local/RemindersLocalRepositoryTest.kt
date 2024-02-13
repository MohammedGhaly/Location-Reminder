package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get : Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository : RemindersLocalRepository
    private lateinit var database : RemindersDatabase

    @Before
    fun setup(){
        // preparing a place for Room database in the memory and initializing the repository
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java).build()
        repository = RemindersLocalRepository(database.reminderDao())
    }

    // deleting the database from memory when test ends
    @After
    fun cleanUp() = database.close()

    @Test
    fun addReminder_getItById() = runBlocking{
        // Given a reminder is saved to the repository
        val reminder1 = ReminderDTO("reminder1", "first reminder", "reminder location", 1.0, 2.0, "reminder1 id")
        repository.saveReminder(reminder1)
        // When the repository retrieves that reminder
        val result = repository.getReminder(reminder1.id)
        // Then it gets the same data of that reminder correctly
        if (result is Result.Success){
            val res = result.data
            assertThat(res.title, `is`(reminder1.title))
            assertThat(res.description, `is`(reminder1.description))
            assertThat(res.id , `is`(reminder1.id))
            assertThat(res.latitude, `is`(reminder1.latitude))
            assertThat(res.longitude, `is`(reminder1.longitude))
            assertThat(res.location, `is`(reminder1.location))
        }
    }

    @Test
    fun getNonExistentReminder_returnsError() = runBlocking {
        // Given a reminder is saved to the repository
        val reminder1 = ReminderDTO("reminder1", "first reminder", "", 1.0, 2.0, "reminder1 id")
        repository.saveReminder(reminder1)
        // When the repository tries to get a reminder with false id
        val result = repository.getReminder("another id")
        // Then the result is an Error with the relevant message
        if (result is Result.Error)
            assertThat(result as Result.Error, `is`(Result.Error("Reminder not found!")))
    }

    @Test
    fun deleteReminders_returnsEmptyList() = runBlocking{
        // Given a number of reminders is saved to the repository
        val reminder1 = ReminderDTO("reminder1", "first reminder", "reminder1 location", 1.0, 2.0, "reminder1 id")
        val reminder2 = ReminderDTO("reminder2", "second reminder", "reminder2 location", 3.0, 4.0, "reminder2 id")
        val reminder3 = ReminderDTO("reminder3", "third reminder", "reminder3 location", 5.0, 6.0, "reminder3 id")
        repository.saveReminder(reminder1)
        repository.saveReminder(reminder2)
        repository.saveReminder(reminder3)
        // When the repository gets a list of all reminders
        val result = repository.getReminders()
        // Then the list returned has all the reminders saved before
        if (result is Result.Success)
            assertThat(result.data.size , `is`(3))


        // When the repository deletes All Reminders
        repository.deleteAllReminders()
        // Then the returned list from getReminders is EMPTY
        val res = repository.getReminders()
        if (res is Result.Success)
            assertThat(res.data.size , `is`(0))

    }

}