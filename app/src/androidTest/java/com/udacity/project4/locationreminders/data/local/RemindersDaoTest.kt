package com.udacity.project4.locationreminders.data.local

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.RemindersDatabase

import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsNull
import org.junit.*
import org.robolectric.annotation.Config

@Config(maxSdk = Build.VERSION_CODES.P, minSdk = Build.VERSION_CODES.P) // Value of Build.VERSION_CODES.P is 28
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get : Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database :RemindersDatabase

    @Before
    fun initDb(){
        // preparing a place for Room database in the memory
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java)
            .build()
    }

    // deleting the database from memory when test ends
    @After
    fun cleanUp() = database.close()

    @Test
    fun insertReminderAndGetItById() = runBlockingTest {
        // Given that the database has saved a new reminder
        val reminder1 = ReminderDTO("reminder1", "first reminder", "reminder1 location", 1.0, 2.0, "reminder1 id")
        database.reminderDao().saveReminder(reminder1)
        // When the dao retrieves that reminder
        val loaded = database.reminderDao().getReminderById(reminder1.id)
        // Then it gets the same data that was saved
        Assert.assertThat(loaded as ReminderDTO , IsNull.notNullValue())
        Assert.assertThat(loaded.title , `is`(reminder1.title))
        Assert.assertThat(loaded.description , `is`(reminder1.description))
        Assert.assertThat(loaded.id , `is`(reminder1.id))
        Assert.assertThat(loaded.location , `is`(reminder1.location))
        Assert.assertThat(loaded.latitude , `is`(reminder1.latitude))
        Assert.assertThat(loaded.longitude , `is`(reminder1.longitude))
    }
}