package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsNull.nullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@Config(maxSdk = Build.VERSION_CODES.P, minSdk = Build.VERSION_CODES.P) // Value of Build.VERSION_CODES.P is 28
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get : Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get : Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel : SaveReminderViewModel
    private lateinit var dataSource : FakeDataSource

    @Before
    fun setup(){
        dataSource = FakeDataSource()
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @After
    fun endTest(){
        stopKoin()
    }

    @Test
    fun loading() = runBlocking {
        // Given that the dispatcher is paused
        mainCoroutineRule.pauseDispatcher()
        val reminder1 = ReminderDataItem("reminder1", "first reminder", "reminder1 location", 1.0, 2.0, "reminder1 id")
        // When the viewModel tries to load save a new reminder to the data source
        viewModel.validateAndSaveReminder(reminder1)
        // Then the viewModel will be held Loading and will not get the value
        assertThat(viewModel.showLoading.getOrAwaitValue() , `is`(true))
    }

    @Test
    fun errorEmptyLocation() = runBlocking {
        // Given that the reminder has empty location field
        val reminder1 = ReminderDataItem("reminder1", "first reminder", "", 1.0, 2.0, "reminder1 id")
        // When the viewModel tries to validate the reminder info
        viewModel.validateAndSaveReminder(reminder1)
        // Then there will be an error snackBar with the specified content
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(),`is`(com.udacity.project4.R.string.err_select_location))
    }

    @Test
    fun errorEmptyTitle() = runBlocking {
        // Given that the reminder has empty title field
        val reminder1 = ReminderDataItem("", "first reminder", "reminder1 location", 1.0, 2.0, "reminder1 id")
        // When the viewModel tries to validate the reminder info
        viewModel.validateAndSaveReminder(reminder1)
        // Then there will be an error snackBar with the specified content
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(),`is`(com.udacity.project4.R.string.err_enter_title))
    }

    @Test
    fun onClearLiveDataFields() = runBlocking {
        // Given that the liveData fields are not empty
        viewModel.reminderTitle.value = "reminder title"
        viewModel.reminderDescription.value = "reminder description"
        viewModel.reminderSelectedLocationStr.value = "reminder location"
        viewModel.selectedPOI.value = PointOfInterest(LatLng(20.0, 30.0),"pointOfInterest","pointOfInterest")   // fake info
        viewModel.latitude.value = 20.0
        viewModel.longitude.value = 40.0
        // When the viewModel decides to clear the liveData
        viewModel.onClear()
        // Then all liveData fields will be null
        assertThat(viewModel.reminderTitle.value,                   `is`(nullValue()))
        assertThat(viewModel.reminderDescription.value,             `is`(nullValue()))
        assertThat(viewModel.reminderSelectedLocationStr.value,     `is`(nullValue()))
        assertThat(viewModel.selectedPOI.value,                     `is`(nullValue()))
        assertThat(viewModel.latitude.value,                        `is`(nullValue()))
        assertThat(viewModel.longitude.value,                       `is`(nullValue()))
    }
}