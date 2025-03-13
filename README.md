# Nextbillion.AI Android Navigation SDK DEMO App

## Installing and Running the project
Before running this project, make sure you have configured your own API Key in 
`NavApplication`

`Nextbillion.getInstance(getApplicationContext(), "Your Api Key");`

## How to integrate Navigation SDK in your project
1. Add the navigation SDK dependency in your app level build.gradle file
    ```gradle
    implementation 'ai.nextbillion:nb-navigation-android:2.0.0''
    ```
2. Add the following permissions in your AndroidManifest.xml file
    ```xml
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    ```
3. Set up the Navigation SDK in your Application class
    ```java
    Nextbillion.getInstance(getApplicationContext(), "Your Api Key");`
    ```
4. Start the Navigation SDK
   You can start the Navigation SDK by referring to the code snippet in the `NavigationMapsActivity` class of this project.