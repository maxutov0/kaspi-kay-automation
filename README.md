# kaspi-pay-automation

## Extract apk using script
```bash
adb kill-server
adb start-server

adb devices

adb shell pm list packages | grep kaspi  

adb shell pm path hr.asseco.android.kaspibusiness

adb pull /data/app/~~zI1hsLDyG-33wYwZUW9k6Q==/hr.asseco.android.kaspibusiness-jCAdMl6jJ3--VaWKWL0Keg==/base.apk
adb pull /data/app/~~zI1hsLDyG-33wYwZUW9k6Q==/hr.asseco.android.kaspibusiness-jCAdMl6jJ3--VaWKWL0Keg==/split_config.arm64_v8a.apk
adb pull /data/app/~~zI1hsLDyG-33wYwZUW9k6Q==/hr.asseco.android.kaspibusiness-jCAdMl6jJ3--VaWKWL0Keg==/split_config.xhdpi.apk
```

## Install apk to emulator
```bash
adb install-multiple -r ./base.apk ./split_config.arm64_v8a.apk ./split_config.xhdpi.apk
```

## Start appium server (local version)
```sh
yarn run appium
```

## Build the instrumentation test APK
```sh
./gradlew assembleAndroidTest 
```

## Move build to root directory
```sh
mv ./app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk ./instrumentation-test.apk
```

## Install instrumentation test APK
```sh
echo "Installing instrumentation test APK..."
adb install -r ./instrumentation-test.apk
```

## Install x86_64 emulation
```sh
sudo dpkg --add-architecture amd64
sudo apt-get update
sudo apt-get install -y libc6:amd64 libstdc++6:amd64
```

### MacOS (m)
```sh
sudo apt-get install -y qemu-user-static
```

## Cron (env depends on shell)
```sh
* * * * * /bin/bash -c '/usr/local/bin/adb shell am instrument -w -r -e debug false -e class "hehe.miras.kaspibusinesstest.KaspiBusinessTest" hehe.miras.kaspibusinesstest.test/androidx.test.runner.AndroidJUnitRunner' >> ~/adb_cron.log 2>&1
```