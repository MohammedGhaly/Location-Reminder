# Location Reminder

- This is a reminder app that allows a user to create and save a reminder when they arrive a at location;
- Users select and save locations by setting map markers on a map;
- When the user reaches a the location they have saved in their reminder, the app will send a notification with the remidner details;
- The app requires the user to create an account and login to set and access reminders;
- The app fully supports data peristence using user preferences and local database.

# Core Technologies

- FirebaseUI (Authentication with custom layout)

- Room (Local Database)

- Google Maps

- Geofences, Location

- Notifications

- ViewModel, LiveDaya (MVVM)

- Koin

- Android Test / Test
  - Espresso (UI Test Framework)
  - Roboelectric (Integration Test Framework)
  - Junit, Mockito (Unit Test Framework)

# Setup

## Installation

This project's repository can be cloned via git or downloaded as a zip file.

## Google Maps API Key

This app requires a Google Maps API key. There are 2 parts to adding a Google Maps API key to the project as follows:

- Google Maps Platform:

  - Register a free Google Maps API key by following the instructions at: https://developers.google.com/maps/documentation/android-sdk/get-api-key
  - Copy the API key once generated

- In the project Repo:
  - Open the AndroidManifest.xml file,
  - Replace meta-data tag > android:value "${GOOGLE_MAPS_API_KEY}" with "@string/google_maps_key"
  - Open the file "google_maps_api.xml" in res > values,
  - Replace the text API_KEY_HERE with the copied Google Maps API Key,
  - Rebuild the project

## Project Dependencies

### App dependencies

implementation "androidx.appcompat:appcompat:$appCompatVersion"
implementation "androidx.legacy:legacy-support-v4:$androidXLegacySupport"
implementation "androidx.annotation:annotation:$androidXAnnotations"
implementation "androidx.cardview:cardview:$cardVersion"
implementation "com.google.android.material:material:$materialVersion"
implementation "androidx.recyclerview:recyclerview:$recyclerViewVersion"
implementation "androidx.constraintlayout:constraintlayout:$constraintVersion"
implementation 'com.google.code.gson:gson:2.8.5'

### Navigation dependencies

implementation 'androidx.appcompat:appcompat:1.3.1'
implementation 'androidx.constraintlayout:constraintlayout:2.1.0'

### Android Lifecycle

kapt "androidx.lifecycle:lifecycle-compiler:$archLifecycleVersion"
implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:$archLifecycleVersion"
implementation "androidx.lifecycle:lifecycle-livedata-ktx:$archLifecycleVersion"

### navigation

implementation "androidx.navigation:navigation-fragment-ktx:$navigationVersion"
implementation "androidx.navigation:navigation-ui-ktx:$navigationVersion"

### Room dependencies

implementation "androidx.room:room-ktx:$roomVersion"
implementation "androidx.room:room-runtime:$roomVersion"
kapt "androidx.room:room-compiler:$roomVersion"

### Coroutines Dependencies

implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion"

### Koin

implementation "org.koin:koin-android:$koinVersion"
implementation "org.koin:koin-androidx-viewmodel:$koinVersion"
androidTestImplementation('org.koin:koin-test:2.0.1') { exclude group: 'org.mockito' }

### Dependencies for local unit tests

testImplementation "junit:junit:$junitVersion"
testImplementation "org.hamcrest:hamcrest-all:$hamcrestVersion"
testImplementation "androidx.arch.core:core-testing:$archTestingVersion"
testImplementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion"
testImplementation "org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion"
testImplementation "org.robolectric:robolectric:$robolectricVersion"
testImplementation "com.google.truth:truth:$truthVersion"
testImplementation "org.mockito:mockito-core:$mockitoVersion"

### AndroidX Test - JVM testing

testImplementation "androidx.test:core-ktx:$androidXTestCoreVersion"
testImplementation "androidx.test.ext:junit-ktx:$androidXTestExtKotlinRunnerVersion"
testImplementation "androidx.test:rules:$androidXTestRulesVersion"

### AndroidX Test - Instrumented testing

androidTestImplementation "androidx.test:core-ktx:$androidXTestCoreVersion"
androidTestImplementation "androidx.test.ext:junit-ktx:$androidXTestExtKotlinRunnerVersion"
androidTestImplementation "org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion"
androidTestImplementation "androidx.test:rules:$androidXTestRulesVersion"
androidTestImplementation "androidx.room:room-testing:$roomVersion"
androidTestImplementation "androidx.arch.core:core-testing:$archTestingVersion"
androidTestImplementation "org.robolectric:annotations:$robolectricVersion"
androidTestImplementation('androidx.test:runner:1.4.0')
androidTestImplementation('androidx.test:rules:1.4.0')

### Espresso UI

implementation "androidx.test.espresso:espresso-idling-resource:$espressoVersion"
androidTestImplementation "androidx.test.espresso:espresso-core:$espressoVersion"
androidTestImplementation "androidx.test.espresso:espresso-contrib:$espressoVersion"
androidTestImplementation "androidx.test.espresso:espresso-intents:$espressoVersion"
androidTestImplementation "androidx.test.espresso.idling:idling-concurrent:$espressoVersion"
androidTestImplementation "junit:junit:$junitVersion"
debugImplementation "androidx.fragment:fragment-testing:$fragmentVersion"
implementation "androidx.test:core:$androidXTestCoreVersion"
implementation "androidx.fragment:fragment-ktx:$fragmentVersion"

### Mockito mock testing

androidTestImplementation "org.mockito:mockito-core:$mockitoVersion"
androidTestImplementation "com.linkedin.dexmaker:dexmaker-mockito:$dexMakerVersion"

### Maps, Places & Geofencing

implementation "com.google.android.gms:play-services-location:$playServicesVersion"
implementation 'com.google.android.gms:play-services-maps:17.0.1'
implementation 'com.google.android.gms:play-services-places:17.0.0'

### Firebase

implementation platform('com.google.firebase:firebase-bom:28.4.0')
implementation 'com.firebaseui:firebase-ui-auth:5.0.0'

### Overcome single dex file limits

implementation("androidx.multidex:multidex:$multidex_version")

# Testing

This app has fully implemented androidTest and test pacakages
To run, right click on the `test` or `androidTest` packages and select Run Tests

## List of available Tests

### androidTest

#### 1. Data Tests

- FakeAndroidTestRespository (for mocking the local repository / datasource)

- RemidnersDAOTest

  - insertReminderAndGetById

- RemindersLocalRepository Test
  - saveReminderDto_retrievesReminderDto
  - insertThreeRemindersAndGetAllReminders
  - insertThreeRemindersAndDeleteAllReminders
  - getReminderByIdWhenReminderDoesNotExist

#### 2. UI Tests

- RemindersListFragmentTest

  - clickAddReminderFab_navigateToSaveReminderFragment
  - savedReminder_checkReminderTextIsDisplayedInUi
  - emptyReminderList_showNoDataMessage

- SaveReminderFragmentTest

  - noReminderTitleAndClickSaveFab_showSnackBarError
  - noReminderLocationAndClickSaveFab_showSnackBarError

- RemindersActivityTest (end to end test)
  - startActivity_checkAddReminderFlow

### test

- FakeTestRespository (for mocking the local repository / datasource)

- ReminderListViewModelTest

  - loadReminders_showLoading
  - loadRemindersWhenRemindersAreUnavailable_showSnackBar
  - loadRemindersWithNoReminders_showNoData
  - loadRemindersWithReminders_showNoData

- SaveReminderViewModelTest
  - saveReminder_showLoading
  - createReminderWithNullTitle_validateAndSaveReminder
  - createReminderWithNullLocation_validateAndSaveReminder
  - saveReminder_showToast
  - saveReminder_navigateBack()
  - createReminder_checkViewModelLiveDataValues

## Built Using

- [Android Studio](https://developer.android.com/studio) - Default IDE used to build android apps
- [Kotlin](https://kotlinlang.org/) - Default language used to build this project

## Deployment information

- <strong>Deployment Target (android API / Version):</strong> 30 / Android 11 (R)
