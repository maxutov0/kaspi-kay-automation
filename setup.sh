#!/bin/bash

# Функция для логирования
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# Остановить выполнение при ошибке
set -e

# Установка cpu-checker и проверка KVM
log "🛠️ Шаг 1: Установка cpu-checker и проверка поддержки KVM..."
apt-get update
apt-get install -y cpu-checker  # Установка cpu-checker
if grep -q -E 'vmx|svm' /proc/cpuinfo; then
    log "✅ KVM поддерживается процессором."
    if kvm-ok 2>/dev/null; then
        log "✅ KVM доступен и готов к использованию."
    else
        log "⚠️ KVM не настроен. Установите KVM для аппаратного ускорения."
        log "🛠️ Установка KVM..."
        apt-get update
        apt-get install -y qemu-kvm libvirt-daemon-system libvirt-clients bridge-utils virt-manager
        usermod -aG kvm $USER
        usermod -aG libvirt $USER
        log "✅ KVM установлен и настроен. Перезагрузите сервер для применения изменений."
        exit 1  # Остановка скрипта, так как требуется перезагрузка
    fi
else
    log "❌ KVM не поддерживается процессором. Эмулятор будет работать без аппаратного ускорения."
    log "⚠️ Для корректной работы эмулятора требуется поддержка KVM. Скрипт остановлен."
    exit 1  # Остановка скрипта
fi

# Установка OpenJDK 17
if ! command -v java &> /dev/null; then
    log "🛠️ Шаг 2: Установка OpenJDK 17..."
    export DEBIAN_FRONTEND=noninteractive
    apt-get update 
    apt-get install -y --no-install-recommends openjdk-17-jdk 
    log "✅ OpenJDK 17 успешно установлен."
else
    log "✅ OpenJDK 17 уже установлен."
fi

# Настройка переменной JAVA_HOME
log "🛠️ Шаг 3: Настройка переменной JAVA_HOME..."
JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
export JAVA_HOME
export PATH=$JAVA_HOME/bin:$PATH
log "✅ Переменная JAVA_HOME настроена: $JAVA_HOME"

# Установка зависимостей
log "🛠️ Шаг 4: Установка зависимостей..."
apt-get update 
apt-get install -y unzip wget 
log "✅ Зависимости успешно установлены."

# Создание директории SDK
if [ ! -d "/opt/android-sdk/cmdline-tools" ]; then
    log "🛠️ Шаг 5: Создание директории SDK..."
    mkdir -p /opt/android-sdk/cmdline-tools
    cd /opt/android-sdk/cmdline-tools
    log "✅ Директория /opt/android-sdk/cmdline-tools создана."
else
    log "✅ Директория /opt/android-sdk/cmdline-tools уже существует."
fi

# Скачивание и распаковка Android SDK
if [ ! -d "/opt/android-sdk/cmdline-tools/latest" ]; then
    log "🛠️ Шаг 6: Скачивание Android SDK..."
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip -O cmdline-tools.zip
    unzip -o cmdline-tools.zip 
    rm cmdline-tools.zip
    mv cmdline-tools latest
    log "✅ Android SDK успешно скачан и распакован."
else
    log "✅ Android SDK уже установлен."
fi

# Принятие лицензий SDK
log "🛠️ Шаг 7: Принятие лицензий Android SDK..."
yes | /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager --licenses 
log "✅ Лицензии успешно приняты."

# Установка платформ и инструментов
log "🛠️ Шаг 8: Установка платформ и инструментов Android SDK..."
/opt/android-sdk/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-34" 
log "✅ Платформы и инструменты успешно установлены."

# Установка AAPT2
if [ ! -f "/opt/android-sdk/build-tools/34.0.0/aapt2" ]; then
    log "🛠️ Шаг 9: Установка AAPT2..."
    /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager "build-tools;34.0.0" 
    log "✅ AAPT2 успешно установлен."
else
    log "✅ AAPT2 уже установлен."
fi

# Установка эмулятора
log "🛠️ Шаг 10: Установка эмулятора Android..."
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

# Установка системного образа, если он отсутствует
SYSTEM_IMAGE="system-images;android-34;google_apis;x86_64"
if ! /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager --list | grep -q "$SYSTEM_IMAGE"; then
    log "🛠️ Шаг 11: Установка системного образа $SYSTEM_IMAGE..."
    /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager "$SYSTEM_IMAGE" 
    log "✅ Системный образ $SYSTEM_IMAGE успешно установлен."
else
    log "✅ Системный образ $SYSTEM_IMAGE уже установлен."
fi

# Создание виртуального устройства
AVD_NAME="pixel_7"
DEVICE="pixel_7"

if ! /opt/android-sdk/cmdline-tools/latest/bin/avdmanager list avd | grep -q "$AVD_NAME"; then
    log "🛠️ Шаг 12: Создание виртуального устройства..."
    /opt/android-sdk/cmdline-tools/latest/bin/avdmanager create avd \
        --name "$AVD_NAME" \
        --package "$SYSTEM_IMAGE" \
        --device "$DEVICE" \
        --force 
    log "✅ Виртуальное устройство $AVD_NAME создано успешно."
else
    log "✅ Виртуальное устройство $AVD_NAME уже существует."
fi

# Проверка и установка cron, если он отсутствует
log "🛠️ Шаг #: Проверка наличия cron..."
if ! command -v cron &> /dev/null; then
    log "⚠️ Cron не установлен. Установка cron..."
    apt-get update
    apt-get install -y cron
    systemctl start cron
    systemctl enable cron
    log "✅ Cron успешно установлен и запущен."
else
    log "✅ Cron уже установлен."
fi

# Путь к файлу с задачей cron
CRON_FILE="./crontab"  # Замените на путь к вашему файлу
CRON_DEST="/etc/cron.d/kaspi_test"  # Путь, куда будет скопирован файл

# Копирование файла с задачей cron, если он еще не скопирован
log "🛠️ Шаг #: Копирование файла с задачей cron..."
if [ ! -f "$CRON_DEST" ]; then
    if [ -f "$CRON_FILE" ]; then
        cp "$CRON_FILE" "$CRON_DEST"
        chmod 644 "$CRON_DEST"  # Установка правильных прав для файла cron
        log "✅ Файл с задачей cron успешно скопирован в $CRON_DEST."
    else
        log "❌ Файл $CRON_FILE не найден. Проверьте путь к файлу."
        exit 1
    fi
else
    log "✅ Файл с задачей cron уже существует в $CRON_DEST."
fi

# Очистка кеша
log "🛠️ Шаг 13: Очистка кеша..."
apt-get clean 
rm -rf /var/lib/apt/lists/* 
log "✅ Кеш успешно очищен."

# Установка графического окружения
log "🛠️ Шаг 14: Установка графического окружения..."
apt-get update
apt-get install -y xfce4 xterm
log "✅ Графическое окружение установлено."

# Настройка файла ~/.xsession
log "🛠️ Шаг 15: Настройка файла ~/.xsession..."
echo "startxfce4" > ~/.xsession
chmod +x ~/.xsession
log "✅ Файл ~/.xsession настроен."

# Установка VNC-сервера
log "🛠️ Шаг 16: Установка VNC-сервера..."
apt-get update 
apt-get install -y tightvncserver 
log "✅ VNC-сервер успешно установлен."

# Настройка VNC-сервера
log "🛠️ Шаг 17: Настройка VNC-сервера..."
mkdir -p ~/.vnc
echo "password" | vncpasswd -f > ~/.vnc/passwd
chmod 600 ~/.vnc/passwd
log "✅ Пароль для VNC установлен."

# Перезапуск VNC-сервера
log "🛠️ Шаг 18: Перезапуск VNC-сервера..."
vncserver -kill :1 > /dev/null 2>&1 || true
vncserver :1 -geometry 1280x800 -depth 24
log "✅ VNC-сервер перезапущен."

# Установка noVNC
log "🛠️ Шаг 19: Установка noVNC..."
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
log "🛠️ Шаг 20: Настройка noVNC..."
ln -sf /opt/noVNC/vnc_lite.html /opt/noVNC/index.html
log "✅ Настройка noVNC завершена."

# Настройка systemd для автозапуска noVNC
log "🛠️ Шаг 21: Настройка автозапуска noVNC..."
NOVNC_SERVICE="/etc/systemd/system/novnc.service"

if [ ! -f "$NOVNC_SERVICE" ]; then
    cat <<EOF | sudo tee "$NOVNC_SERVICE" > /dev/null
[Unit]
Description=noVNC
After=network.target

[Service]
ExecStart=/opt/noVNC/utils/novnc_proxy --vnc localhost:5901 --listen 0.0.0.0:6080
Restart=always
User=root

[Install]
WantedBy=multi-user.target
EOF

    sudo systemctl daemon-reload
    sudo systemctl start novnc
    sudo systemctl enable novnc
    log "✅ Автозапуск noVNC настроен."
else
    log "✅ Автозапуск noVNC уже настроен."
fi

# Открытие порта 6080
log "🛠️ Шаг 22: Открытие порта 6080..."
ufw allow 6080/tcp
log "✅ Порт 6080 открыт."

# Получение внешнего IP-адреса сервера
log "🛠️ Шаг 23: Получение внешнего IP-адреса сервера..."
PUBLIC_IP=$(curl -s ifconfig.me)
if [ -z "$PUBLIC_IP" ]; then
    log "❌ Не удалось получить внешний IP-адрес."
    exit 1
else
    log "✅ Внешний IP-адрес сервера: $PUBLIC_IP"
fi

# Запуск Android-эмулятора
log "🛠️ Шаг 24: Запуск Android-эмулятора..."
/opt/android-sdk/emulator/emulator -avd "$AVD_NAME" -no-audio -no-boot-anim -no-window &
EMULATOR_PID=$!
log "✅ Android-эмулятор запущен (PID: $EMULATOR_PID)."

# Ожидание загрузки эмулятора
log "🛠️ Шаг 25: Ожидание загрузки эмулятора..."
sleep 30  # Подождите 30 секунд для загрузки эмулятора
log "✅ Эмулятор загружен и готов к использованию."

# Инструкции для подключения
log "🎉 Подключение к VNC через браузер:"
echo "1. Откройте в браузере: http://$PUBLIC_IP:6080/vnc.html"
echo "2. Введите пароль: password"
echo "3. Наслаждайтесь удалённым доступом!"