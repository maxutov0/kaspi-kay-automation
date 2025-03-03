#!/bin/bash

set -e  # Останавливает выполнение скрипта при ошибке

function run_tests() {
    # 1. Билдит проект
    echo "Building main application and instrumentation test..."
    ./gradlew assembleDebug assembleAndroidTest
    echo "Build completed."

    # 2. Устанавливает на эмуляторе, удаляя старые версии
    echo "Uninstalling old versions if they exist..."
    adb uninstall hehe.miras.kaspibusinesstest || true  # Удаляет основное приложение (если есть)
    adb uninstall hehe.miras.kaspibusinesstest.test || true  # Удаляет тестовое приложение (если есть)

    echo "Installing new versions..."
    adb install -r ./app/build/outputs/apk/debug/app-debug.apk  # Устанавливает основное приложение
    adb install -r ./app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk  # Устанавливает тестовое приложение
    echo "Installation completed."

    # 3. Запускает тесты и показывает логи в реальном времени
    echo "Starting instrumentation tests..."
    adb shell am instrument -w -r -e debug false -e class "hehe.miras.kaspibusinesstest.KaspiBusinessTest" hehe.miras.kaspibusinesstest.test/androidx.test.runner.AndroidJUnitRunner &

    # Запускает логи в реальном времени в фоновом режиме
    echo "Starting logcat in real-time..."
    adb logcat -c  # Очищает старые логи
    adb logcat | grep -E "KaspiBusinessTest|AndroidJUnitRunner" &
    LOGCAT_PID=$!  # Сохраняем PID процесса logcat
}

# Основной цикл
while true; do
    run_tests

    # Ожидание ввода пользователя
    echo "Press 'R' to restart the script or any other key to exit..."
    read -n 1 input

    # Останавливаем logcat перед перезапуском
    kill $LOGCAT_PID 2>/dev/null || true

    if [[ "$input" != "r" && "$input" != "R" ]]; then
        break
    fi
done

echo "Script terminated."