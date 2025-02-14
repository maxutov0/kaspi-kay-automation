#!/bin/bash

set -e 

echo "Building main application..."
./gradlew assembleDebug
echo "Main application built"

echo "Building instrumentation test..."
./gradlew assembleAndroidTest
echo "Instrumentation test built"

mv ./app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk ./instrumentation-test.apk
mv ./app/build/outputs/apk/debug/app-debug.apk ./app-debug.apk
