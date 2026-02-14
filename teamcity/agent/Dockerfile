FROM jetbrains/teamcity-agent:latest

USER root

# Install JDK 25 (Early Access)
RUN apt-get update && \
    apt-get install -y wget && \
    wget -q https://download.oracle.com/java/25/latest/jdk-25_linux-x64_bin.tar.gz -O /tmp/jdk25.tar.gz && \
    mkdir -p /usr/lib/jvm/jdk-25 && \
    tar -xzf /tmp/jdk25.tar.gz -C /usr/lib/jvm/jdk-25 --strip-components=1 && \
    rm /tmp/jdk25.tar.gz && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

ENV JDK_25_x64=/usr/lib/jvm/jdk-25
ENV JAVA_HOME=/usr/lib/jvm/jdk-25
ENV PATH="${JAVA_HOME}/bin:${PATH}"

USER buildagent
