#!/bin/bash

#
# Copyright (C) 2026 yuriamw (https://github.com/yuriamw)
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see <https://www.gnu.org/licenses/>.
#

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

KEYSTORE=~/devel/amw/android/amweather-release.jks
BUILD_TOOLS=~/Android/Sdk/build-tools/37.0.0
UNSIGNED="$PROJECT_DIR/app/build/outputs/apk/release/app-release-unsigned.apk"

VERSION=$(grep 'versionName' "$PROJECT_DIR/app/build.gradle.kts" | grep -o '"[^"]*"' | tr -d '"')
OUTPUT="$PROJECT_DIR/app/build/outputs/apk/release/amweather-${VERSION}.apk"

echo "Building release APK..."
"$PROJECT_DIR/gradlew" -p "$PROJECT_DIR" assembleRelease

echo "Signing amweather-${VERSION}.apk..."
"$BUILD_TOOLS/apksigner" sign \
    --ks "$KEYSTORE" \
    --out "$OUTPUT" \
    "$UNSIGNED"

echo "Verifying..."
"$BUILD_TOOLS/apksigner" verify --verbose "$OUTPUT"

echo "Done: $OUTPUT"
