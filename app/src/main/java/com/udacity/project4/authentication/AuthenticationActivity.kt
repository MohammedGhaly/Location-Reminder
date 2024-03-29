package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.map
import androidx.lifecycle.observe
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object{
        const val SIGN_IN_RESULT_CODE = 9939
        const val TAG = "AuthenticationActivity"
    }

    private lateinit var binding : ActivityAuthenticationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.activity_authentication, null, false)
        setContentView(binding.root)

        fun launchSignInFlow() {
            // Give users the option to sign in with their email or Google account
            val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build()
                ,AuthUI.IdpConfig.GoogleBuilder().build()
            )

            // Create sign-in intent. We listen to the response of this activity with the
            // SIGN_IN_RESULT_CODE code.
            startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), SIGN_IN_RESULT_CODE
            )
        }

        binding.logInBtn.setOnClickListener {
            // launches the sign in process
            launchSignInFlow()
        }

        val authenticationState = FirebaseUserLiveData().map { user ->
            if (user != null)
                AuthenticationState.AUTHENTICATED
            else
                AuthenticationState.UNAUTHENTICATED
        }

        authenticationState.observe(this){
            when(it){
                AuthenticationState.AUTHENTICATED ->{
                    // if the user authenticated successfully navigate to the reminders activity
                    val intent = Intent(this, RemindersActivity::class.java)
                    startActivity(intent)
                }
                AuthenticationState.UNAUTHENTICATED ->{}
            }
        }
    }

    override fun onActivityResult(requestCode : Int , resultCode : Int , data : Intent?) {
        super.onActivityResult(requestCode , resultCode , data)
        if (requestCode == SIGN_IN_RESULT_CODE){
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in user.
                Log.i(TAG, "Successfully signed in user " + "${FirebaseAuth.getInstance().currentUser?.displayName}!")
            } else {
                // Sign in failed. If response is null the user canceled the sign-in flow using
                // the back button. Otherwise check response.getError().getErrorCode() and handle
                // the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }
}
