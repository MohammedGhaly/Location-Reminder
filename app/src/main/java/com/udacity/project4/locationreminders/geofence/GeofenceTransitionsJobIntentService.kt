package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        // code triggered when the user enters the geofencing area
        val geoFenceEvent= GeofencingEvent.fromIntent(intent)
        when (geoFenceEvent.geofenceTransition) {
            // send a notification to the user when he enters the geofence area
            Geofence.GEOFENCE_TRANSITION_ENTER ->
                sendNotification(geoFenceEvent.triggeringGeofences)
        }
    }

    private fun sendNotification(triggeringGeofences: List<Geofence>) {
        // get the notification for every reminder with the geofence entered
        for(item in triggeringGeofences) {
            val requestId = item.requestId

            // Get the local repository instance
            // Interaction to the repository has to be through a coroutine scope
            val remindersLocalRepository : ReminderDataSource by inject()
            CoroutineScope(coroutineContext).launch(SupervisorJob()) {
                //get the reminder with the request id
                val result = remindersLocalRepository.getReminder(requestId)
                if (result is Result.Success<ReminderDTO>) {
                    val reminder = result.data
                    //send a notification to the user with the reminder details
                    sendNotification(
                        this@GeofenceTransitionsJobIntentService ,
                        ReminderDataItem(
                            reminder.title ,
                            reminder.description ,
                            reminder.location ,
                            reminder.latitude ,
                            reminder.longitude ,
                            reminder.id
                        )
                    )
                }
            }
        }
    }

}
