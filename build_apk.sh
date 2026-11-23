#!/bin/bash
set -e

echo "======================================================"
echo "🛡️  Phone Guardian - Android APK Builder on Termux"
echo "======================================================"

# 1. Update and install basic dependencies
echo ">> [1/5] Checking essential packages (JDK 17, Gradle, tools)..."
apt update -y
apt install -y openjdk-17-jdk wget curl unzip git

export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-arm64"
export PATH="$JAVA_HOME/bin:$PATH"

# 2. Setup Android Command Line Tools / SDK
export ANDROID_HOME="/root/android-sdk"
export ANDROID_SDK_ROOT="/root/android-sdk"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

if [ ! -d "$ANDROID_HOME/platforms/android-34" ]; then
    echo ">> [2/5] Setting up Android SDK Command-line Tools..."
    mkdir -p "$ANDROID_HOME/cmdline-tools"
    
    if [ ! -f "/root/cmdline-tools.zip" ]; then
        echo ">> Downloading Android Command Line Tools..."
        wget -q --show-progress -O /root/cmdline-tools.zip "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
    fi
    
    unzip -q -o /root/cmdline-tools.zip -d "$ANDROID_HOME/cmdline-tools"
    mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest" 2>/dev/null || true
    
    echo ">> [3/5] Installing Android Platform 34 and Build-Tools..."
    yes | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --licenses > /dev/null 2>&1 || true
    "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" "platforms;android-34" "build-tools;34.0.0" "platform-tools"
else
    echo ">> Android SDK already configured."
fi

# Create local.properties
echo "sdk.dir=$ANDROID_HOME" > local.properties

# 3. Build APK
echo ">> [4/5] Compiling Phone Guardian APK (Gradle assembleDebug --no-daemon)..."
chmod +x ./gradlew
./gradlew assembleDebug --no-daemon --stacktrace

# 4. Copy to Downloads folder
echo ">> [5/5] Copying compiled APK to /sdcard/Download..."
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    cp app/build/outputs/apk/debug/app-debug.apk /sdcard/Download/PhoneGuardian.apk
    echo "======================================================"
    echo "🎉 تم بناء تطبيق Phone Guardian بنجاح!"
    echo "📁 مسار الملف: /sdcard/Download/PhoneGuardian.apk"
    echo "يمكنك الآن فتح مدير الملفات (Files) وتثبيت التطبيق مباشرة!"
    echo "======================================================"
else
    echo "❌ خطأ: لم يتم العثور على ملف APK الناتج."
    exit 1
fi
