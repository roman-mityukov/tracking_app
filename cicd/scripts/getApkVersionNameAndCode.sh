#!/bin/bash

# Останов если нет аргумента
if [ -z "$1" ]; then
    echo "Usage: $0 <path-to-apk>"
    exit 1
fi

# Останов если по аргументу нет файла
APK_PATH="$1"
if [ ! -f "$APK_PATH" ]; then
    echo "Error: APK file not found at '$APK_PATH'"
    exit 1
fi

# Ищем в системе команду aapt и выбираем первую найденную
AAPT=$(command -v aapt || command -v "$ANDROID_HOME"/build-tools/*/aapt | head -n 1)

# Останов если нет aapt в системе
if [ -z "$AAPT" ]; then
    echo "Error: aapt not found. Make sure Android SDK is installed and in PATH."
    exit 1
fi

VERSION_NAME=$("$AAPT" dump badging "$APK_PATH" | grep -oP "versionName='\K[^']+")
VERSION_CODE=$("$AAPT" dump badging "$APK_PATH" | grep -oP "versionCode='\K[^']+")

echo "$VERSION_NAME($VERSION_CODE)"