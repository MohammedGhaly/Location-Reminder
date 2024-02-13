package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"
        private var coroutineJob: Job = Job()
        val coroutineContext: CoroutineContext
            get() = Dispatchers.IO + coroutineJob

        // receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            deleteReminderFromDatabase(reminderDataItem, context)
            return intent
        }

        private fun deleteReminderFromDatabase(reminder : ReminderDataItem, context : Context){
            // deletes the reminder from database on Reminder completion
            val dao = LocalDB.createRemindersDao(context)
            CoroutineScope(coroutineContext).launch(SupervisorJob()) {
                dao.deleteReminderById(reminder.id)
            }
        }

    }

    private lateinit var binding: ActivityReminderDescriptionBinding
    private lateinit var geofenceClient : GeofencingClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminder_description)

        geofenceClient = LocationServices.getGeofencingClient(this)
        val reminder = intent.getSerializableExtra(EXTRA_ReminderDataItem) as ReminderDataItem

        binding.reminderDataItem = reminder

        deleteGeofence(reminder.id)
    }

    private fun deleteGeofence(id : String) {
        geofenceClient.removeGeofences(listOf(id))?.run {
            addOnSuccessListener {
                Toast.makeText(applicationContext, "Geofence removed successfully", Toast.LENGTH_LONG).show()
            }

            addOnFailureListener{
                Toast.makeText(applicationContext, "Geofence couldn't be removed", Toast.LENGTH_LONG).show()
            }
        }
    }

//    fun deleteReminderFromDatabase(reminder : ReminderDataItem){
//        val dao = LocalDB.createRemindersDao(this)
//        lifecycleScope.launch{
//                dao.deleteReminderById(reminder.id)
//        }
//    }
}
