package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.runner.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeAndroidDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragmentDirections
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class ReminderListFragmentTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repo : FakeAndroidDataSource
    private lateinit var viewModel: RemindersListViewModel
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun setup(){
        repo = FakeAndroidDataSource()
        viewModel = RemindersListViewModel(getApplicationContext(), repo)
        saveReminderViewModel = SaveReminderViewModel(getApplicationContext(), repo)
        stopKoin()
        val myModule = module {
            single {
                viewModel
            }
            single {
                saveReminderViewModel
            }
        }
        startKoin {
            modules(listOf(myModule))
        }
    }

    @Test
    fun clickOnFAB_navigateToSaveRemindersFragment(){
        // Given
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        // When the floating action button is clicked
        onView(withId(R.id.addReminderFAB)).perform(click())
        // Then the app navigates to saveReminderFragment
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun clickOnSelectingReminderLocation_navigateToSelectLocationFragment(){
        // Given
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        // When the select Location button is clicked
        onView(withId(R.id.selectLocation)).perform(click())
        // Then the app navigates to SelectLocationFragment
        verify(navController).navigate(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
    }

    @Test
    fun displayEmptyRemindersList(){
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

    @Test
    fun addNewReminder_checkItIsDisplayed() = runBlockingTest {
        val reminder = ReminderDTO("reminder1", "first reminder", "reminder1 location", 1.0, 2.0, "reminder1 id")
        repo.saveReminder(reminder)
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withText(reminder.title)).check(matches(isDisplayed()))
    }

    @Test
    fun deleteExistingReminder_checkItIsRemoved() = runBlockingTest {
        val reminder = ReminderDTO("reminder1", "first reminder", "reminder1 location", 1.0, 2.0, "reminder1 id")
        repo.saveReminder(reminder)
        repo.deleteAllReminders()
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

    @After
    fun cleanUp() = stopKoin()

}