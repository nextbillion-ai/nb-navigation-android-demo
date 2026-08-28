# Custom speech integration

The custom speech example is intentionally separated from the app's general navigation example:

- `CustomSpeechActivity` fetches a route and launches the example.
- `CustomSpeechNavigationActivity` owns the `NavigationView`, permission handling, and custom speech configuration.
- `speech/` contains the Android TTS player and audio-focus implementation.
- `activity_custom_speech.xml` and `activity_custom_speech_navigation.xml` are dedicated layouts for the example.

## Files to copy

Copy these files into the corresponding packages in your application:

1. `activity/CustomSpeechActivity.java`
2. `activity/CustomSpeechNavigationActivity.java`
3. All Java files under `speech/`
4. `layout/activity_custom_speech.xml`
5. `layout/activity_custom_speech_navigation.xml`
6. The `custom_speech_*` string resources used by the two activities

Register the navigation activity in your manifest:

```xml
<activity
    android:name=".activity.CustomSpeechNavigationActivity"
    android:configChanges="orientation|screenSize|keyboardHidden"
    android:exported="false" />
```

The application also needs internet and foreground location permissions, SDK initialization in
its `Application`, and the Navigation SDK dependency shown in the root README.

## Key integration point

Create the custom player and pass it to `NavViewConfig` before starting navigation:

```java
SpeechPlayer speechPlayer = createSpeechPlayer(resolveRouteLanguage());

NavViewConfig config = NavViewConfig.builder()
        .route(route)
        .routes(routes)
        .speechPlayer(speechPlayer)
        .build();

navigationView.startNavigation(config);
```

`CustomSpeechNavigationActivity` is the complete reference for forwarding the activity lifecycle
to `NavigationView` and requesting location permission before calling `initialize`.
