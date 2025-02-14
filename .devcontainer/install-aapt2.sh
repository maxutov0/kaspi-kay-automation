#!/bin/bash

set -e  # Остановит выполнение при ошибке

echo "🔍 Проверяем установку Android SDK и AAPT2..."

# Проверяем, существует ли aapt2
if [ -f "/opt/android-sdk/build-tools/34.0.0/aapt2" ]; then
    echo "✅ AAPT2 уже установлен."
else
    echo "⚠️ AAPT2 не найден! Устанавливаем..."

    # Проверяем, существует ли build-tools
    if [ -d "/opt/android-sdk/build-tools/android-14" ]; then
        echo "📦 Перемещаем build-tools в правильную директорию..."
        sudo mv /opt/android-sdk/build-tools/android-14 /opt/android-sdk/build-tools/34.0.0
    else
        echo "📥 Скачиваем и устанавливаем build-tools..."
        wget -q https://dl.google.com/android/repository/build-tools_r34-linux.zip -O /tmp/build-tools.zip
        unzip -q /tmp/build-tools.zip -d /opt/android-sdk/build-tools/
        sudo mv /opt/android-sdk/build-tools/android-14 /opt/android-sdk/build-tools/34.0.0
        rm /tmp/build-tools.zip
    fi
fi

# Определяем архитектуру
ARCH=$(dpkg --print-architecture)

echo "🔍 Определена архитектура: $ARCH"

if [ "$ARCH" = "amd64" ]; then
    echo "📦 Устанавливаем QEMU и 64-битные библиотеки для x86_64..."
    sudo apt-get update && sudo apt-get install -y qemu-user-static binfmt-support libc6:amd64 libstdc++6:amd64 zlib1g:amd64
elif [ "$ARCH" = "arm64" ]; then
    echo "⚠️ ARM64-архитектура: устанавливаем только QEMU без x86_64-библиотек..."
    sudo dpkg --add-architecture amd64
    sudo apt-get update
    sudo apt-get install -y qemu-user-static binfmt-support libc6:amd64 libstdc++6:amd64 zlib1g:amd64

    echo "🔍 Проверяем наличие ld-linux-x86-64.so.2..."
    if [ ! -f "/lib64/ld-linux-x86-64.so.2" ]; then
        echo "🔗 Создаём символическую ссылку..."
        sudo ln -s /lib/x86_64-linux-gnu/ld-linux-x86-64.so.2 /lib64/
    fi
else
    echo "❌ Ошибка: неподдерживаемая архитектура ($ARCH)" >&2
    exit 1
fi

# Проверяем работу AAPT2
echo "🔧 Проверяем запуск AAPT2 через QEMU..."
if /usr/bin/qemu-x86_64-static -L /lib/x86_64-linux-gnu /opt/android-sdk/build-tools/34.0.0/aapt2 version; then
    echo "✅ AAPT2 работает корректно!"
else
    echo "❌ Ошибка: AAPT2 не запускается!" >&2
    exit 1
fi

# Добавляем alias, если его нет
if ! grep -q "aapt2" ~/.bashrc; then
    echo "🔧 Добавляем alias для AAPT2..."
    echo "alias aapt2='/usr/bin/qemu-x86_64-static -L /lib/x86_64-linux-gnu /opt/android-sdk/build-tools/34.0.0/aapt2'" >> ~/.bashrc
    source ~/.bashrc
fi

echo "🎉 Установка завершена! Теперь можно использовать 'aapt2 version'"