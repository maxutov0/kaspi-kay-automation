#!/bin/bash

adb kill-server
adb start-server

adb devices
# TODO: check if authorized
# List of devices attached
# RZ8R71MGVCN     unauthorized

# search for package
adb shell pm list packages | grep kaspi  
# package:hr.asseco.android.kaspibusiness

# get path 
adb shell pm path hr.asseco.android.kaspibusiness
# package:/data/app/~~bLtTFAoQYdv0jlcxlWw91A==/hr.asseco.android.kaspibusiness-S63KlQlKyR7Pj9UHt2uLgQ==/base.apk
# package:/data/app/~~bLtTFAoQYdv0jlcxlWw91A==/hr.asseco.android.kaspibusiness-S63KlQlKyR7Pj9UHt2uLgQ==/split_config.arm64_v8a.apk
# package:/data/app/~~bLtTFAoQYdv0jlcxlWw91A==/hr.asseco.android.kaspibusiness-S63KlQlKyR7Pj9UHt2uLgQ==/split_config.xhdpi.apk

# pull apk
adb pull /data/app/~~zI1hsLDyG-33wYwZUW9k6Q==/hr.asseco.android.kaspibusiness-jCAdMl6jJ3--VaWKWL0Keg==/base.apk
adb pull /data/app/~~zI1hsLDyG-33wYwZUW9k6Q==/hr.asseco.android.kaspibusiness-jCAdMl6jJ3--VaWKWL0Keg==/split_config.arm64_v8a.apk
adb pull /data/app/~~zI1hsLDyG-33wYwZUW9k6Q==/hr.asseco.android.kaspibusiness-jCAdMl6jJ3--VaWKWL0Keg==/split_config.xhdpi.apk

# /data/app/~~zI1hsLDyG-33wYwZUW9k6Q==/hr.asseco.android.kaspibusiness-jCAdMl6jJ3--VaWKWL0Keg==/base.apk: 1 file pulled, 0 skipped. 23.6 MB/s (64142373 bytes in 2.588s)
# /data/app/~~zI1hsLDyG-33wYwZUW9k6Q==/hr.asseco.android.kaspibusiness-jCAdMl6jJ3--VaWKWL0Keg==/split_config.arm64_v8a.apk: 1 file pulled, 0 skipped. 24.0 MB/s (33387744 bytes in 1.326s)
# /data/app/~~zI1hsLDyG-33wYwZUW9k6Q==/hr.asseco.android.kaspibusiness-jCAdMl6jJ3--VaWKWL0Keg==/split_config.xhdpi.apk: 1 file pulled, 0 skipped. 21.8 MB/s (882571 bytes in 0.039s)