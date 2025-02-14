#!/bin/bash

set -e

# check available devices
adb devices

# Install KaspiPay if not installed
if ! adb shell pm list packages | grep -q hr.asseco.android.kaspibusiness; then
  echo "Installing KaspiPay"
  adb install-multiple -r ./base.apk ./split_config.arm64_v8a.apk ./split_config.xhdpi.apk
  echo "KaspiPay installed"
fi

# Удаляем и Устанавливаем основное приложение (app-debug.apk)
echo "Uninstalling the main application"
adb uninstall hehe.miras.kaspibusinesstest
echo "The main application uninstalled"
echo "Installing the main application"
adb install -r ./app-debug.apk
echo "The main application installed"

# Удаляем и Устанавливаем тестовое приложение (instrumentation-test.apk)
echo "Uninstalling the test application"
adb uninstall hehe.miras.kaspibusinesstest.test
echo "The test application uninstalled"
echo "Installing the test application"
adb install -r ./instrumentation-test.apk
echo "The test application installed"