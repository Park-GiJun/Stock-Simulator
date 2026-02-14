FROM jetbrains/teamcity-agent:latest

USER root

# Install JDK 21
RUN apt-get update && \
    apt-get install -y wget && \
    wget -q https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.tar.gz -O /tmp/jdk21.tar.gz && \
    mkdir -p /usr/lib/jvm/jdk-21 && \
    tar -xzf /tmp/jdk21.tar.gz -C /usr/lib/jvm/jdk-21 --strip-components=1 && \
    rm /tmp/jdk21.tar.gz && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

ENV JDK_21_x64=/usr/lib/jvm/jdk-21
ENV JAVA_HOME=/usr/lib/jvm/jdk-21
ENV PATH="${JAVA_HOME}/bin:${PATH}"

USER buildagent
