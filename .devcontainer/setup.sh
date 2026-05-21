#!/bin/bash
set -e

echo "================================================"
echo "  JARVIS - Setting up Android Development Environment"
echo "================================================"

# Set permissions
echo ""
echo "Setting up Gradle wrapper..."
chmod +x gradlew 2>/dev/null || true

# Ensure gradle wrapper directory exists
mkdir -p gradle/wrapper

# Verify Java
echo ""
echo "Checking Java version:"
java -version 2>&1 || echo "⚠ Java not found - will be installed by features"

# Verify ANDROID_HOME and install SDK if needed
echo ""
echo "Checking Android SDK..."

if [ -d "$ANDROID_HOME" ]; then
    echo "✓ ANDROID_HOME = $ANDROID_HOME"
    
    # Find sdkmanager
    SDKMANAGER=""
    if [ -f "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" ]; then
        SDKMANAGER="$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager"
    elif [ -f "$ANDROID_HOME/tools/bin/sdkmanager" ]; then
        SDKMANAGER="$ANDROID_HOME/tools/bin/sdkmanager"
    elif [ -f "$ANDROID_HOME/cmdline-tools/bin/sdkmanager" ]; then
        SDKMANAGER="$ANDROID_HOME/cmdline-tools/bin/sdkmanager"
    fi
    
    if [ -n "$SDKMANAGER" ]; then
        echo "Found sdkmanager at: $SDKMANAGER"
        
        # Accept licenses (non-interactive)
        yes | $SDKMANAGER --licenses 2>/dev/null || true
        
        # Install required SDK components
        echo "Installing Android SDK components..."
        $SDKMANAGER "platforms;android-34" 2>&1 | tail -5 || true
        $SDKMANAGER "build-tools;34.0.0" 2>&1 | tail -5 || true
        $SDKMANAGER "platform-tools" 2>&1 | tail -5 || true
        
        # Verify installations
        if [ -d "$ANDROID_HOME/platforms/android-34" ]; then
            echo "✓ Android SDK 34 installed"
        fi
        if [ -d "$ANDROID_HOME/build-tools/34.0.0" ]; then
            echo "✓ Build tools 34.0.0 installed"
        fi
    else
        echo "⚠ sdkmanager not found - checking if SDK is pre-installed..."
        ls "$ANDROID_HOME/" 2>/dev/null || echo "  Android SDK directory empty"
    fi
else
    echo "⚠ ANDROID_HOME not set or not found at $ANDROID_HOME"
    echo "  Checking alternatives..."
    
    # Check common Android SDK locations
    for dir in /usr/local/lib/android/sdk /opt/android-sdk $HOME/Android/Sdk; do
        if [ -d "$dir" ]; then
            echo "  Found Android SDK at: $dir"
            export ANDROID_HOME="$dir"
            break
        fi
    done
fi

# Create local.properties
echo ""
echo "Creating local.properties..."
echo "sdk.dir=$ANDROID_HOME" > local.properties
echo "✓ local.properties created"

# Generate Gradle wrapper if jar is missing
echo ""
if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "Generating Gradle wrapper JAR..."
    if command -v gradle &> /dev/null; then
        echo "  Using Gradle: $(gradle --version 2>&1 | head -1)"
        gradle wrapper --gradle-version 8.7 2>&1 || true
        if [ -f "gradle/wrapper/gradle-wrapper.jar" ]; then
            echo "✓ Gradle wrapper JAR generated successfully"
        else
            echo "⚠ Could not generate wrapper JAR - downloading directly..."
            GRADLE_WRAPPER_URL="https://github.com/gradle/gradle/raw/v8.7.0/gradle/wrapper/gradle-wrapper.jar"
            curl -sL "$GRADLE_WRAPPER_URL" -o gradle/wrapper/gradle-wrapper.jar 2>/dev/null || \
            wget -q "$GRADLE_WRAPPER_URL" -O gradle/wrapper/gradle-wrapper.jar 2>/dev/null || \
            echo "⚠ Could not download gradle-wrapper.jar"
        fi
    else
        echo "⚠ Gradle not found - downloading gradle-wrapper.jar directly..."
        GRADLE_WRAPPER_URL="https://github.com/gradle/gradle/raw/v8.7.0/gradle/wrapper/gradle-wrapper.jar"
        curl -sL "$GRADLE_WRAPPER_URL" -o gradle/wrapper/gradle-wrapper.jar 2>/dev/null || \
        wget -q "$GRADLE_WRAPPER_URL" -O gradle/wrapper/gradle-wrapper.jar 2>/dev/null || \
        echo "⚠ Could not download gradle-wrapper.jar"
    fi
fi

# Verify gradle wrapper jar exists
if [ -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "✓ gradle-wrapper.jar ready"
else
    echo "⚠ gradle-wrapper.jar missing - build will attempt to generate it"
fi

# Make gradlew executable
chmod +x gradlew 2>/dev/null || true

echo ""
echo "================================================"
echo "  Setup Complete!"
echo "  Run './gradlew assembleDebug' to build APK"
echo "================================================"
