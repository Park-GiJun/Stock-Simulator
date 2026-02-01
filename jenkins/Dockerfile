FROM jenkins/jenkins:lts-jdk17

USER root

# Install dependencies
RUN apt-get update && \
    apt-get install -y wget tar && \
    apt-get clean

# Download and install Java 25 manually
RUN wget https://download.oracle.com/java/25/latest/jdk-25_linux-x64_bin.tar.gz && \
    mkdir -p /usr/lib/jvm && \
    tar -xzf jdk-25_linux-x64_bin.tar.gz -C /usr/lib/jvm && \
    rm jdk-25_linux-x64_bin.tar.gz && \
    mv /usr/lib/jvm/jdk-25* /usr/lib/jvm/jdk-25

# Set JAVA_HOME for Java 25
ENV JAVA_HOME=/usr/lib/jvm/jdk-25
ENV PATH=$JAVA_HOME/bin:$PATH

# Verify Java installation
RUN java -version

# Install Docker CLI
RUN apt-get update && \
    apt-get install -y docker.io && \
    apt-get clean

USER jenkins
