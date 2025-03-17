#!/bin/bash

# Функция для логирования
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# Остановить выполнение при ошибке
set -e

# Настройка переменных окружения
log "🛠️ Настройка переменных окружения..."
export DISPLAY=:1
export QT_QPA_PLATFORM=offscreen
export PATH=$PATH:/opt/android-sdk/platform-tools  # Добавление adb в PATH
log "✅ Переменные окружения настроены."

# Установка недостающих библиотек
log "🛠️ Установка недостающих библиотек..."
sudo apt-get update
sudo apt-get install -y libxcb-cursor0 libxcb-xinerama0 libxcb-icccm4 libxcb-keysyms1 libxcb-image0 libxcb-render-util0 libxcb-xkb1 libxkbcommon-x11-0
log "✅ Библиотеки установлены."

# Проверка, запущен ли уже эмулятор
if pgrep -f "emulator" > /dev/null; then
    log "❌ Эмулятор уже запущен. Остановите его перед повторным запуском."
    exit 1
fi

# Остановка VNC-сервера, если он уже запущен
log "🛠️ Остановка VNC-сервера, если он уже запущен..."
vncserver -kill :1 > /dev/null 2>&1 || true
log "✅ VNC-сервер остановлен."

# Проверка занятости порта 6080
if lsof -i :6080 > /dev/null; then
    log "❌ Порт 6080 уже занят. Освободите порт и попробуйте снова."
    exit 1
fi

# Запуск VNC-сервера
log "🛠️ Запуск VNC-сервера..."
vncserver :1 -geometry 1280x800 -depth 24
log "✅ VNC-сервер запущен."

# Запуск noVNC
log "🛠️ Запуск noVNC..."
/opt/noVNC/utils/novnc_proxy --vnc localhost:5901 --listen 0.0.0.0:6080 &
NOVNC_PID=$!
log "✅ noVNC запущен (PID: $NOVNC_PID)."

# Получение внешнего IP-адреса сервера
log "🛠️ Получение внешнего IP-адреса сервера..."
PUBLIC_IP=$(curl -s ifconfig.me)
if [ -z "$PUBLIC_IP" ]; then
    log "❌ Не удалось получить внешний IP-адрес."
    exit 1
else
    log "✅ Внешний IP-адрес сервера: $PUBLIC_IP"
fi

# Инструкции для подключения
log "🎉 Подключение к VNC через браузер:"
echo "1. Откройте в браузере: http://$PUBLIC_IP:6080/vnc.html"
echo "2. Введите пароль: password"
echo "3. Наслаждайтесь удалённым доступом!"

# Запуск Android-эмулятора
log "🛠️ Запуск Android-эмулятора..."
/opt/android-sdk/emulator/emulator -avd "pixel_7" -no-audio -no-boot-anim -gpu swiftshader_indirect -read-only -no-metrics &
EMULATOR_PID=$!
log "✅ Android-эмулятор запущен (PID: $EMULATOR_PID)."

# Ожидание загрузки эмулятора
log "🛠️ Ожидание загрузки эмулятора..."
sleep 180  # Увеличено время ожидания до 180 секунд
log "✅ Эмулятор загружен и готов к использованию."

# Перезапуск adb
log "🛠️ Перезапуск adb..."
adb kill-server
adb start-server
log "✅ adb перезапущен."

# Проверка доступности эмулятора через adb
log "🛠️ Проверка доступности эмулятора через adb..."
adb devices | grep "emulator" || {
    log "❌ Эмулятор не обнаружен через adb. Проверьте логи эмулятора."
    exit 1
}
log "✅ Эмулятор обнаружен через adb."

# Ожидание завершения работы
wait $EMULATOR_PID