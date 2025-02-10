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

## Test with youtube first

