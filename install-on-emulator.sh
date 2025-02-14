#!/bin/bash

set -e

# check available devices
adb devices

# Install KaspiPay
echo "Installing KaspiPay"
adb install-multiple -r ./base.apk ./split_config.arm64_v8a.apk ./split_config.xhdpi.apk
echo "KaspiPay installed"

# Устанавливаем основное приложение (app-debug.apk)
echo "Installing main application"
adb install -r ./app-debug.apk
echo "Main application installed"

# Install the app on the emulator
echo "Installing the app on the emulator"
adb install -r ./instrumentation-test.apk
echo "The app installed on the emulator"