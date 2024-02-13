package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.provider.Settings
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig.APPLICATION_ID
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.EspressoIdlingResource.wrapEspressoIdlingResource
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var reminder : ReminderDataItem
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q


    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(activity, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        // Use FLAG_UPDATE_CURRENT so that you get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = _viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            // Navigate to another fragment to get the user location
                navigateToSelectLocationFragment()
        }

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        binding.saveReminder.setOnClickListener {

            if (!_viewModel.validateEnteredData())
                return@setOnClickListener

            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value
            reminder = ReminderDataItem(title, description, location, latitude, longitude)

            requestForegroundAndBackgroundLocationPermissions()
        }
    }

    private fun navigateToSelectLocationFragment() {
            _viewModel.navigationCommand.value = NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
    }

    override fun onDestroy() {
        super.onDestroy()
        //making sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence(){
        wrapEspressoIdlingResource {
            val geofence = Geofence.Builder()
                .setRequestId(reminder.id)
                .setCircularRegion(reminder.latitude!!,
                    reminder.longitude!!,
                    GEOFENCE_RADIUS_IN_METERS    // radius in meters
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnSuccessListener {
                    _viewModel.validateAndSaveReminder(reminder)
                }
                .addOnFailureListener{
                    checkDeviceLocationSettingsAndStartGeofencing()
                }
        }
    }


    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions() {

        if (foregroundAndBackgroundLocationPermissionApproved()){
            checkDeviceLocationSettingsAndStartGeofencing()
            return
        } else if (runningQOrLater && foregroundPermissionOnlyApproved()){
            showRequestBackgroundLocationSnackbar()
        } else{
            // request foreground permissions only
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION ,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) , REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun requestBackGroundPermission(){
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION) ,
            REQUEST_BACKGROUND_PERMISSION_RESULT_CODE
        )
    }

    private fun foregroundPermissionOnlyApproved() : Boolean {
        val foregroundLocationApproved = (
            PackageManager.PERMISSION_GRANTED ==
                ActivityCompat.checkSelfPermission(activity !! , Manifest.permission.ACCESS_FINE_LOCATION))
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                    ActivityCompat.checkSelfPermission(activity !! , Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else {
                true
            }
        return foregroundLocationApproved && !backgroundPermissionApproved
    }

    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
            PackageManager.PERMISSION_GRANTED ==
                ActivityCompat.checkSelfPermission(activity !! , Manifest.permission.ACCESS_FINE_LOCATION))
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                    ActivityCompat.checkSelfPermission(activity !! , Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    private fun checkDeviceLocationSettingsAndStartGeofencing(resolve:Boolean = true) {

        if (isLocationEnabled()){
            addGeofence()
            return
        }

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if ((exception is ResolvableApiException) && resolve){
                try {
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        REQUEST_TURN_DEVICE_LOCATION_ON,
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            }
        }
    }

    private fun isLocationEnabled() : Boolean {
        val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode : Int , permissions : Array<out String> , grantResults : IntArray) {

        if(requestCode == REQUEST_BACKGROUND_PERMISSION_RESULT_CODE){
            if ( grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_DENIED)
            { //  background permission is denied
                showPermissionDeniedSnackbar()
            } else
            {// background permission granted
                checkDeviceLocationSettingsAndStartGeofencing()
            }

        } else if (requestCode == REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE){
            if ( grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_DENIED)
            { // foreground permission denied
                showPermissionDeniedSnackbar()
            } else
            { // foreground permission granted
                showRequestBackgroundLocationSnackbar()
            }
        }
    }

    private fun showRequestBackgroundLocationSnackbar() {
        Snackbar.make(binding.root, "you should grant location permission all the time", Snackbar.LENGTH_LONG)
            .setAction("Grant permission"){
                requestBackGroundPermission()
            }.show()
    }

    override fun onActivityResult(requestCode : Int , resultCode : Int , data : Intent?) {
        super.onActivityResult(requestCode , resultCode , data)
        when (requestCode) {
            REQUEST_TURN_DEVICE_LOCATION_ON -> when (resultCode) {
                AppCompatActivity.RESULT_OK -> {

                    // All required changes were successfully made
                    try{
                        lifecycleScope.launch{
                            repeat(10){
                                if (isLocationEnabled()){
                                    checkDeviceLocationSettingsAndStartGeofencing()
                                    return@repeat
                                }
                                else
                                    delay(25)
                            }
                        }
                    }
                    catch (e : Exception){
                        Log.i(TAG, "${e.message}")
                    }
                }
                AppCompatActivity.RESULT_CANCELED -> {
                    // The user was asked to change settings, but chose not to
                  showEnableLocationSnackBar()
                }
            }
        }
    }

    private fun showEnableLocationSnackBar(){
        Snackbar.make( binding.root, context!!.getString(R.string.location_required_error), Snackbar.LENGTH_LONG)
            .setAction("enable location") { checkDeviceLocationSettingsAndStartGeofencing() }.show()
    }

    companion object{
        val ACTION_GEOFENCE_EVENT = "ACTION_GEOFENCE_EVENT"
    }

    private fun showPermissionDeniedSnackbar() {
        Snackbar.make(
            binding.root , R.string.permission_denied_explanation , Snackbar.LENGTH_INDEFINITE
        ).setAction(R.string.settings) {
            startActivity(Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package" , APPLICATION_ID , null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }.show()
    }
}

private  val GEOFENCE_RADIUS_IN_METERS = 100f
private  val REQUEST_BACKGROUND_PERMISSION_RESULT_CODE = 33
private  val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private  val TAG = "save"
private  val REQUEST_TURN_DEVICE_LOCATION_ON = 29


