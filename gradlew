#!/bin/bash
set -e

GRADLE_VERSION="8.5"
GRADLE_DIR="/root/.gradle-dist/gradle-$GRADLE_VERSION"

if [ ! -x "$GRADLE_DIR/bin/gradle" ]; then
    echo ">> [Phone Guardian] Installing Gradle $GRADLE_VERSION for modern Android support..."
    mkdir -p "/root/.gradle-dist"
    if [ ! -f "/root/gradle-$GRADLE_VERSION-bin.zip" ]; then
        echo ">> Downloading Gradle $GRADLE_VERSION binary..."
        wget -q --show-progress -O "/root/gradle-$GRADLE_VERSION-bin.zip" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
    fi
    unzip -q -o "/root/gradle-$GRADLE_VERSION-bin.zip" -d "/root/.gradle-dist"
fi

exec "$GRADLE_DIR/bin/gradle" "$@"
