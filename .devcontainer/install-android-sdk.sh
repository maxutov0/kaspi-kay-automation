#!/bin/bash

# Установить зависимости
apt-get update && apt-get install -y unzip wget

# Создать директорию SDK
mkdir -p /opt/android-sdk/cmdline-tools
cd /opt/android-sdk/cmdline-tools

# Скачать SDK
wget -q https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip -O cmdline-tools.zip
unzip cmdline-tools.zip
mv cmdline-tools latest
rm cmdline-tools.zip

# Установить платформы и инструменты
yes | /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager --licenses
/opt/android-sdk/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-34"

# Очистка кеша
apt-get clean && rm -rf /var/lib/apt/lists/*