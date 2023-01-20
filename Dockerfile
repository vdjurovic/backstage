
# install JDK
FROM ubuntu:22.04 as jdk-setup

ENV JDK_RELEASE 17.0.3+17
ENV JDK_TARBALL jdk-17.tar.gz

RUN apt update && apt install -y wget
RUN wget https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.3%2B7/OpenJDK17U-jdk_x64_linux_hotspot_17.0.3_7.tar.gz -O /opt/$JDK_TARBALL && \
    cd /opt && tar xvzf $JDK_TARBALL && rm /opt/$JDK_TARBALL

FROM ubuntu:22.04 as go-setup
RUN apt update && apt install -y wget
RUN  wget https://go.dev/dl/go1.18.2.linux-amd64.tar.gz && rm -rf /usr/local/go && tar -C /usr/local -xzf go1.18.2.linux-amd64.tar.gz

FROM ubuntu:22.04 as main

ENV JDK_RELEASE 17.0.3+17

RUN apt update && apt install -y nsis rpm make hfsprogs
COPY --from=jdk-setup /opt/ /opt
RUN mkdir /usr/local/go
COPY --from=go-setup /usr/local/go/ /usr/local/go

