FROM mcr.microsoft.com/devcontainers/base:jammy

# Install required packages
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    wget \
    curl \
    unzip \
    git \
    openssh-client \
    ca-certificates \
    software-properties-common \
    && rm -rf /var/lib/apt/lists/*

# Install Java 17 (JDK)
RUN wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor -o /usr/share/keyrings/adoptium.gpg && \
    echo "deb [signed-by=/usr/share/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list && \
    apt-get update && \
    apt-get install -y temurin-17-jdk && \
    rm -rf /var/lib/apt/lists/*

# Verify Java
RUN java --version

# Set JAVA_HOME
ENV JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64

# Install Android SDK command-line tools
RUN mkdir -p /usr/local/android-sdk && \
    cd /usr/local/android-sdk && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip -O cmdline-tools.zip && \
    unzip -q cmdline-tools.zip && \
    rm cmdline-tools.zip && \
    mkdir -p cmdline-tools/latest && \
    mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true && \
    mv cmdline-tools latest/ 2>/dev/null || true

# Set environment variables
ENV ANDROID_HOME=/usr/local/android-sdk
ENV ANDROID_SDK_ROOT=/usr/local/android-sdk
ENV PATH=${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/build-tools/34.0.0
ENV ANDROID_NDK_HOME=${ANDROID_HOME}/ndk/25.2.9519653

# Accept licenses and install SDK components
RUN yes | ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager --licenses && \
    ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager \
    "platform-tools" \
    "platforms;android-34" \
    "build-tools;34.0.0" \
    "ndk;25.2.9519653" \
    "cmdline-tools;latest" \
    "extras;google;m2repository" \
    "extras;android;m2repository"

# Verify installations
RUN java -version && \
    ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager --list 2>/dev/null | head -20

# Create gradle wrapper directory and set permissions
RUN mkdir -p /workspaces/JARVIS/gradle/wrapper

# Set working directory
WORKDIR /workspaces/JARVIS

# Default command
CMD ["/bin/bash"]
