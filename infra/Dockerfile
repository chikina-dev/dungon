FROM eclipse-temurin:21-jre

WORKDIR /data

RUN apt-get update \
 && apt-get install -y curl jq \
 && rm -rf /var/lib/apt/lists/*

RUN curl -L -o /tmp/ProtocolLib.jar https://github.com/dmulloy2/ProtocolLib/releases/download/5.4.0/ProtocolLib.jar

COPY start.sh /start.sh
RUN chmod +x /start.sh

EXPOSE 25565
ENTRYPOINT ["/start.sh"]
