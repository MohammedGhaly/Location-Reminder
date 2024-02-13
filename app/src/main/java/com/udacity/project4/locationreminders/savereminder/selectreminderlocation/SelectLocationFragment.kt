package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.*
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private var map : GoogleMap? = null
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient

    private var selectedLocationAddress = ""
    private var selectedLat : Double? = null
    private var selectedLng : Double? = null

    var isLocationSelected : MutableLiveData<Boolean> = MutableLiveData(false)

    val TAG = "SelectLocationFragment"


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        // used to get the current location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        isLocationSelected.observe(viewLifecycleOwner){
            // changes the button state (clickable or not) when the user selects a location
            changeButtonsState(it)
        }

        binding.saveButton.setOnClickListener {
            if (isLocationSelected.value == true)
                onLocationSelected()
        }
        binding.clearButton.setOnClickListener {
            clearSelectedLocation()
        }

        return binding.root
    }

    private fun onLocationSelected() {
        // fills in the data of needed to create the reminder Ocject
        _viewModel.latitude.value = selectedLat
        _viewModel.longitude.value = selectedLng
        _viewModel.reminderSelectedLocationStr.value = selectedLocationAddress
        // navigating back to the SaveReminderFragment
        _viewModel.navigationCommand.postValue(NavigationCommand.Back)
    }

    private fun onMapClickListener(map : GoogleMap){
        map.setOnMapClickListener{latLng ->
            clearSelectedLocation()     // clearing the previously selected locations
            val poiMarker = map.addMarker(MarkerOptions().position(latLng))    // adding a marker on the location selected
            poiMarker?.showInfoWindow()
            selectLocation(latLng = latLng)
        }
    }

    private fun onPoiClickListener(map : GoogleMap){
        map.setOnPoiClickListener{poi ->
            clearSelectedLocation()         // clearing the previously selected locations
            val poiMarker = map.addMarker(MarkerOptions().position(poi.latLng).title(poi.name))     // adding a marker on the location selected
            poiMarker?.showInfoWindow()
            selectLocation(poi)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // changing the map style on options selected
        R.id.normal_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(p0 : GoogleMap?) {

        map = p0        // initializing the map property
        setMapStyle(map)
        if (!foregroundLocationPermissionApproved())
            showRequestPermissionSnackbar()
        setupMap()
    }

    @SuppressLint("MissingPermission")
    private fun setupMap() {
        map?.let { onPoiClickListener(it) }
        map?.let { onMapClickListener(it) }
        if(foregroundLocationPermissionApproved()){

            if (!isLocationEnabled())
                showEnableLocationSnackbar()

            map?.setMyLocationEnabled(true)         // enabling location tracking
            moveCameraAndSaveLocation()
        }
    }

    private fun showRequestPermissionSnackbar() {
        val action = {requestForegroundLocationPermission()}
        showSnackBar2("it is preferred to grant location permission to see your location", "Grant Permission", action)
    }

    private fun showEnableLocationSnackbar() {
        val action = {
            val locationRequest = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_LOW_POWER
            }
            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            val settingsClient = LocationServices.getSettingsClient(requireActivity())
            val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())

            locationSettingsResponseTask.addOnFailureListener { exception ->
                if ((exception is ResolvableApiException)) {
                    try {
                        startIntentSenderForResult(
                            exception.resolution.intentSender ,
                            REQUEST_TURN_DEVICE_LOCATION_ON ,
                            null ,
                            0 ,
                            0 ,
                            0 ,
                            null
                        )

                    }
                    catch (sendEx : IntentSender.SendIntentException) {
                        Log.d(TAG , "Error getting location settings resolution: " + sendEx.message)
                    }
                }
            }
        }
        showSnackBar("it is preferred to enable the location service to see your location on the map " , action)
    }

    @SuppressLint("MissingPermission")
    private fun moveCameraAndSaveLocation()  {
        fusedLocationProviderClient.lastLocation
            // getting the current location of the user
            .addOnSuccessListener { location : Location? ->
                val preferences = context?.getSharedPreferences(TAG , MODE_PRIVATE)
                // moving the camera of the map to the user location
                if (location != null) {
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location !!.latitude , location !!.longitude) , 18f))
                    // saving the latlng location
                    preferences?.edit()?.putFloat("latitude" , location !!.latitude.toFloat())?.apply()
                    preferences?.edit()?.putFloat("longitude" , location !!.longitude.toFloat())?.apply()
                } else { // moving the camera to the last saved location if the location couldn't be provided
                    val lat = preferences?.getFloat("latitude" , 20f)
                    val lng = preferences?.getFloat("longitude" , 30f)
                    if (lat != 20f) {
                        map?.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    lat !!.toDouble() ,
                                    lng !!.toDouble()
                                ) , 18f
                            )
                        )
                    }
                }
            }
    }

    private fun isLocationEnabled() : Boolean {
        // used to make sure location is enabled before navigating to the SelectLocationFragment
        val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gpsEnabled = false

        gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        return gpsEnabled
    }

    private fun foregroundLocationPermissionApproved(): Boolean{
        val foregroundLocationApproved = (
            PackageManager.PERMISSION_GRANTED ==
            ActivityCompat.checkSelfPermission(activity !! , Manifest.permission.ACCESS_FINE_LOCATION))
        return foregroundLocationApproved
    }

    private fun requestForegroundLocationPermission(){
        val permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        val resultCode = REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        requestPermissions(permissionsArray, resultCode)
    }
    
    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode : Int , permissions : Array<out String> , grantResults : IntArray) {

        if (grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_DENIED)
            showPermissionDeniedSnackbar()
        else
            setupMap()
    }

    override fun onActivityResult(requestCode : Int , resultCode : Int , data : Intent?) {
        super.onActivityResult(requestCode , resultCode , data)
        when (requestCode) {
            REQUEST_TURN_DEVICE_LOCATION_ON -> when (resultCode) {
                AppCompatActivity.RESULT_OK -> {

                    // All required changes were successfully made
                    try{
                        lifecycleScope.launch{
                            for (i in 0..3){
                                delay(1000)
                                if (isLocationEnabled()){
                                    moveCameraAndSaveLocation()
                                }
                            }
                        }
                    }
                    catch (e : Exception){
                        Log.i(TAG , "${e.message}")
                    }
                }
                AppCompatActivity.RESULT_CANCELED -> {
                    // The user was asked to change settings, but chose not to
                    showEnableLocationSnackbar()
                }
            }
        }
    }

    private fun setMapStyle(map : GoogleMap?){
        val success = map?.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireActivity(), R.raw.map_style))
    }

    private fun getAddress(latLng: LatLng): String {
        var strAdd = ""
        // using geocoder to get the address text from the lat and lng info
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val locationList: List<Address>? =
                geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (locationList != null) {
                val address : Address = locationList[0]
                val addressText = StringBuilder("")

                for (i in 0..address.maxAddressLineIndex)
                    addressText.append(address.getAddressLine(i)).append("\n")

                strAdd = addressText.toString()
            }
        } catch (e: java.lang.Exception) {
            Toast.makeText(context, "a problem occurred getting address", Toast.LENGTH_SHORT).show()
            clearSelectedLocation()
        }
        return strAdd
    }

    private fun clearSelectedLocation(){
        // clears the map and the cashing variables used when saving the reminder
        map?.clear()
        selectedLocationAddress = ""
        selectedLat = null
        selectedLng = null
        isLocationSelected.value = false
    }

    // fun takes either a latLng of the selected location if it is not a POI
    // Or takes the POI selected
    private fun selectLocation(poi : PointOfInterest? = null, latLng : LatLng? = null){
        if (poi == null && latLng != null){
            selectedLat = latLng.latitude
            selectedLng = latLng.longitude
            // filling the cashing variable of address using getAddress
            selectedLocationAddress = getAddress(latLng)
            isLocationSelected.value = selectedLocationAddress != ""
        }else
            if (poi != null && latLng == null){
                // filling the cashing variables with data from the POI
                selectedLat = poi.latLng.latitude
                selectedLng = poi.latLng.longitude
                selectedLocationAddress = poi.name
                isLocationSelected.value = true
            }
    }

    private fun changeButtonsState(isLocationSelected : Boolean){
        // changing the Clickable property and the color of the buttons depending on whether the user has selected a location
        if (isLocationSelected){
            binding.saveButton.apply {
                setBackgroundColor(Color.GREEN)
                isClickable = true
            }

            binding.clearButton.apply {
                setBackgroundColor(Color.RED)
                isClickable = true
            }
        }

        else{
            binding.saveButton.apply {
                setBackgroundColor(Color.GRAY)
                isClickable = false
            }

            binding.clearButton.apply {
                setBackgroundColor(Color.GRAY)
                isClickable = false
            }
        }
    }

    private fun showSnackBar(message : String, action : () -> Task<LocationSettingsResponse>) {
        Snackbar.make(
            binding.root , message, Snackbar.LENGTH_LONG
        ).setAction(android.R.string.ok) {
            action()
        }.show()
    }
    
    private fun showSnackBar2(message : String, actionText: String, action : () -> Unit) {
        Snackbar.make(
            binding.root , message, Snackbar.LENGTH_LONG
        ).setAction(actionText) {
            action()
        }.show()
    }

    private fun showPermissionDeniedSnackbar() {

        val action = { startActivity(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package" , BuildConfig.APPLICATION_ID , null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })}

        showSnackBar2(context!!.getString(R.string.permission_denied_explanation),"Settings", action  )
    }

}

private  val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private  val REQUEST_TURN_DEVICE_LOCATION_ON = 29


