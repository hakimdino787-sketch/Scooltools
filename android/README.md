# Scooltools Android app

This native Android shell loads the existing live site at https://myscool.vercel.app/ and places a Google Mobile Ads banner below the web content. Google's User Messaging Platform SDK requests and displays consent where required.

## Build a debug APK

Requires JDK 17 and Android SDK API 35:

```bash
cd android
gradle :app:assembleDebug
```

APK output: `android/app/build/outputs/apk/debug/app-debug.apk`.

## AdMob IDs

This project intentionally defaults to Google's official **test** app and banner IDs. Do not publish a release build with test IDs or click test ads expecting revenue.

After registering Scooltools as its own app in AdMob, build with its app-specific IDs:

```bash
gradle :app:assembleDebug \
  -PADMOB_APP_ID='ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY' \
  -PADMOB_BANNER_ID='ca-app-pub-XXXXXXXXXXXXXXXX/ZZZZZZZZZZ'
```

The App ID and banner unit ID are different values. Do not reuse IDs belonging to another app. Configure the applicable privacy message in AdMob before serving real ads.

## Included

- Arabic RTL website inside Android WebView.
- Android back navigation and file selection for image/video/text/PDF tools.
- Adaptive anchored banner ad at the bottom.
- External links open outside the app.
- No interstitial or app-open ads that interrupt tool usage.
