# MyScool Android app

This native Android shell loads https://myscool.vercel.app/ and displays a Google Mobile Ads adaptive banner below the web content. Google's User Messaging Platform requests and displays consent where required.

## Build a debug APK

Requires JDK 17 and Android SDK API 35:

```bash
cd android
gradle :app:assembleDebug
```

APK output: `android/app/build/outputs/apk/debug/app-debug.apk`.

## AdMob configuration

The default configured IDs are registered for MyScool:

- App ID: `ca-app-pub-2536808054649336~3749576618`
- Banner unit ID: `ca-app-pub-2536808054649336/1123413272`

Use Google's official test IDs for development tests. Do not click your own live ads. Ad delivery may be limited while AdMob reviews the app or account, and the app must have applicable privacy/consent messaging configured.

## Included

- Arabic RTL website inside Android WebView.
- Android back navigation and file selection for image/video/text/PDF tools.
- Adaptive anchored banner ad at the bottom.
- External links open outside the app.
- No interstitial or app-open ads that interrupt tool usage.
