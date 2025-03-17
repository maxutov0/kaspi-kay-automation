#!/bin/bash

# Функция для логирования
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# Остановить выполнение при ошибке
set -e

# Установка OpenJDK 17
if ! command -v java &> /dev/null; then
    log "🛠️ Шаг 1: Установка OpenJDK 17..."
    export DEBIAN_FRONTEND=noninteractive
    apt-get update 
    apt-get install -y --no-install-recommends openjdk-17-jdk 
    log "✅ OpenJDK 17 успешно установлен."
else
    log "✅ OpenJDK 17 уже установлен."
fi

# Настройка переменной JAVA_HOME
log "🛠️ Шаг 2: Настройка переменной JAVA_HOME..."
JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
export JAVA_HOME
export PATH=$JAVA_HOME/bin:$PATH
log "✅ Переменная JAVA_HOME настроена: $JAVA_HOME"

# Установка зависимостей
log "🛠️ Шаг 3: Установка зависимостей..."
apt-get update 
apt-get install -y unzip wget 
log "✅ Зависимости успешно установлены."

# Создание директории SDK
if [ ! -d "/opt/android-sdk/cmdline-tools" ]; then
    log "🛠️ Шаг 4: Создание директории SDK..."
    mkdir -p /opt/android-sdk/cmdline-tools
    cd /opt/android-sdk/cmdline-tools
    log "✅ Директория /opt/android-sdk/cmdline-tools создана."
else
    log "✅ Директория /opt/android-sdk/cmdline-tools уже существует."
fi

# Скачивание и распаковка Android SDK
if [ ! -d "/opt/android-sdk/cmdline-tools/latest" ]; then
    log "🛠️ Шаг 5: Скачивание Android SDK..."
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip -O cmdline-tools.zip
    unzip -o cmdline-tools.zip 
    rm cmdline-tools.zip
    mv cmdline-tools latest
    log "✅ Android SDK успешно скачан и распакован."
else
    log "✅ Android SDK уже установлен."
fi

# Принятие лицензий SDK
log "🛠️ Шаг 6: Принятие лицензий Android SDK..."
yes | /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager --licenses 
log "✅ Лицензии успешно приняты."

# Установка платформ и инструментов
log "🛠️ Шаг 7: Установка платформ и инструментов Android SDK..."
/opt/android-sdk/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-34" 
log "✅ Платформы и инструменты успешно установлены."

# Установка AAPT2
if [ ! -f "/opt/android-sdk/build-tools/34.0.0/aapt2" ]; then
    log "🛠️ Шаг 8: Установка AAPT2..."
    /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager "build-tools;34.0.0" 
    log "✅ AAPT2 успешно установлен."
else
    log "✅ AAPT2 уже установлен."
fi

# Установка эмулятора
log "🛠️ Шаг 9: Установка эмулятора Android..."
/opt/android-sdk/cmdline-tools/latest/bin/sdkmanager "emulator" 
apt-get update 
apt-get install -y \
    libgl1-mesa-dev \
    libpulse0 \
    libx11-dev \
    libxext-dev \
    libxtst-dev \
    libxv-dev \
    libglu1-mesa-dev \
    libncurses6 \
    libcurl4t64 \
    libasound2-dev 
log "✅ Эмулятор успешно установлен."

# Установка системного образа
SYSTEM_IMAGE="system-images;android-34;google_apis;x86_64"
if ! /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager --list_installed | grep -q "$SYSTEM_IMAGE"; then
    log "🛠️ Шаг 10: Установка системного образа $SYSTEM_IMAGE..."
    /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager "$SYSTEM_IMAGE" 
    log "✅ Системный образ $SYSTEM_IMAGE успешно установлен."
else
    log "✅ Системный образ $SYSTEM_IMAGE уже установлен."
fi

# Создание виртуального устройства
AVD_NAME="pixel_7"
DEVICE="pixel_7"

if ! /opt/android-sdk/cmdline-tools/latest/bin/avdmanager list avd | grep -q "$AVD_NAME"; then
    log "🛠️ Шаг 11: Создание виртуального устройства..."
    /opt/android-sdk/cmdline-tools/latest/bin/avdmanager create avd \
        --name "$AVD_NAME" \
        --package "$SYSTEM_IMAGE" \
        --device "$DEVICE" \
        --force 
    log "✅ Виртуальное устройство $AVD_NAME создано успешно."
else
    log "✅ Виртуальное устройство $AVD_NAME уже существует."
fi

# Установка графического окружения
log "🛠️ Шаг 12: Установка графического окружения..."
apt-get update
apt-get install -y xfce4 xterm
log "✅ Графическое окружение установлено."

# Настройка файла ~/.xsession
log "🛠️ Шаг 13: Настройка файла ~/.xsession..."
echo "startxfce4" > ~/.xsession
chmod +x ~/.xsession
log "✅ Файл ~/.xsession настроен."

# Установка VNC-сервера
log "🛠️ Шаг 14: Установка VNC-сервера..."
apt-get update 
apt-get install -y tightvncserver 
log "✅ VNC-сервер успешно установлен."

# Настройка VNC-сервера
log "🛠️ Шаг 15: Настройка VNC-сервера..."
mkdir -p ~/.vnc
echo "password" | vncpasswd -f > ~/.vnc/passwd
chmod 600 ~/.vnc/passwd
log "✅ Пароль для VNC установлен."

# Установка noVNC
log "🛠️ Шаг 16: Установка noVNC..."
apt-get update
apt-get install -y git python3-websockify

# Клонирование noVNC
if [ ! -d "/opt/noVNC" ]; then
    log "🛠️ Клонирование noVNC..."
    git clone https://github.com/novnc/noVNC.git /opt/noVNC
    log "✅ noVNC успешно установлен."
else
    log "✅ noVNC уже установлен."
fi

# Создание символической ссылки для index.html
log "🛠️ Шаг 17: Настройка noVNC..."
ln -sf /opt/noVNC/vnc_lite.html /opt/noVNC/index.html
log "✅ Настройка noVNC завершена."

# Очистка кеша
log "🛠️ Шаг 18: Очистка кеша..."
apt-get clean 
rm -rf /var/lib/apt/lists/* 
log "✅ Кеш успешно очищен."

log "🎉 Установка завершена. Для запуска эмулятора и VNC выполните ./start.sh"