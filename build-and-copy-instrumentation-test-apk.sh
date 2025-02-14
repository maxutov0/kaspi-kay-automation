#!/bin/bash

./gradlew assembleAndroidTest 

mv ./app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk ./instrumentation-test.apk