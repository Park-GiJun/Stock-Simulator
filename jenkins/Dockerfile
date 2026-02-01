FROM jenkins/jenkins:lts-jdk17

USER root

# Install Java 25
RUN apt-get update && \
    apt-get install -y wget && \
    wget https://download.oracle.com/java/25/latest/jdk-25_linux-x64_bin.tar.gz && \
    mkdir -p /opt/java && \
    tar -xzf jdk-25_linux-x64_bin.tar.gz -C /opt/java && \
    rm jdk-25_linux-x64_bin.tar.gz && \
    apt-get clean

# Set JAVA_HOME for Java 25 (Gradle will use this)
ENV JAVA_HOME=/opt/java/jdk-25
ENV PATH=$JAVA_HOME/bin:$PATH

# Install Docker CLI (if not mounted)
RUN apt-get update && \
    apt-get install -y docker.io && \
    apt-get clean

USER jenkins
