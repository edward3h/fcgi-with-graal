# See here for image contents: https://github.com/microsoft/vscode-dev-containers/tree/v0.134.1/containers/ubuntu/.devcontainer/base.Dockerfile
ARG VARIANT="focal"
FROM mcr.microsoft.com/vscode/devcontainers/base:0-${VARIANT}

# [Optional] Uncomment this section to install additional OS packages.
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends bash-completion build-essential tree ack-grep vim libfcgi-perl zlib1g-dev libfcgi-dev

ENV SDKMAN_DIR=/root/.sdkman
RUN  curl -s "https://get.sdkman.io" | bash && \
    echo "sdkman_auto_answer=true" > $SDKMAN_DIR/etc/config && \
    echo "sdkman_auto_selfupdate=false" >> $SDKMAN_DIR/etc/config && \
    echo "sdkman_insecure_ssl=true" >> $SDKMAN_DIR/etc/config && \
    bash -c ". /root/.sdkman/bin/sdkman-init.sh && sdk install java 20.2.0.r11-grl" 
ENV JAVA_HOME=${SDKMAN_DIR}/candidates/java/current
RUN curl -LA gradle-completion https://edub.me/gradle-completion-bash -o /etc/bash_completion.d/gradle-completion.bash
RUN git clone https://github.com/dougborg/gdub.git && \
    cd gdub && \
    ./install
    
RUN ${JAVA_HOME}/bin/gu install native-image