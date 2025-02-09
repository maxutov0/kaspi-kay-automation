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
adb pull /data/app/~~bLtTFAoQYdv0jlcxlWw91A==/hr.asseco.android.kaspibusiness-S63KlQlKyR7Pj9UHt2uLgQ==/base.apk ./kaspi_pay.apk
# /data/app/~~bLtTFAoQYdv0jlcxlWw91A==/hr.asseco.android.kaspibusiness-S63KlQlKyR7Pj9UHt2uLgQ==/base.apk: 1 file pulled, 0 skipped. 26.5 MB/s (64142373 bytes in 2.306s)

