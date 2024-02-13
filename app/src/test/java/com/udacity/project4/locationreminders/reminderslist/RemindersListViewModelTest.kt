package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(maxSdk = Build.VERSION_CODES.P, minSdk = Build.VERSION_CODES.P) // Value of Build.VERSION_CODES.P is 28
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get : Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get : Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel : RemindersListViewModel
    private lateinit var dataSource : FakeDataSource

    @Before
    fun setup(){
        dataSource = FakeDataSource()
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @After
    fun endTest(){
        stopKoin()
    }

    @Test
    fun addReminders_RemindersAdded(){
        val reminder1 = ReminderDTO("reminder1", "first reminder", "reminder1 location", 1.0, 2.0, "reminder1 id")
        val reminder2 = ReminderDTO("reminder2", "second reminder", "reminder2 location", 3.0, 4.0, "reminder2 id")
        val reminder3 = ReminderDTO("reminder3", "third reminder", "reminder3 location", 5.0, 6.0, "reminder3 id")
        // Given a data set bound to the data source
        val remindersList = mutableListOf(reminder1, reminder2, reminder3)
        dataSource = FakeDataSource(remindersList)
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
        // When the viewModel tries to load the reminders from the source
        viewModel.loadReminders()
        // Then the viewModel list is updated successfully with the reminders from the database
        assertThat(viewModel.remindersList.getOrAwaitValue(), (not(emptyList())) )
        assertThat(viewModel.remindersList.getOrAwaitValue().size, `is`(remindersList.size))
    }

    @Test
    fun returnReturnError() {
        // Given an empty data set bound to the data source
        dataSource = FakeDataSource(mutableListOf())
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)

        // When the viewModel tries to load the reminders from the source
        viewModel.loadReminders()
        // Then the viewModel list is empty
        assertThat(viewModel.showSnackBar.getOrAwaitValue(), `is`("there is no active reminders"))

        dataSource.setReturnError(true)
        // When the viewModel tries to load the reminders from the source
        viewModel.loadReminders()
        // Then there would be an error
        assertThat(viewModel.showSnackBar.getOrAwaitValue(), `is`("there is no active reminders"))
    }

    @Test
    fun check_loading() {
        // Given that the dispatcher is paused
        mainCoroutineRule.pauseDispatcher()
        // When the viewModel tries to load the reminders from the source
        viewModel.loadReminders()
        // Then the viewModel will be held loading and will not get the value
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))
        // When the dispatcher is resumed
        mainCoroutineRule.resumeDispatcher()
        // Then the viewModel finishes loading
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

}