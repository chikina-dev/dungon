#!/bin/sh
set -eu

PROJECT=paper
VERSION=1.21.11
E_MAIL_ADDRESS=chikina.lisp.quest@gmail.com
JAR_NAME=paper.jar

# EULA
if [ ! -f eula.txt ]; then
  echo "eula=true" > eula.txt
fi

# ProtocolLib
mkdir -p plugins
cp /tmp/ProtocolLib.jar plugins/ProtocolLib.jar

# Paper 未取得ならダウンロード
if [ ! -f "${JAR_NAME}" ]; then
  DOWNLOAD_URL=$(curl -s \
    -H "User-Agent: Easy Latest version getter (${E_MAIL_ADDRESS})" \
    https://fill.papermc.io/v3/projects/${PROJECT}/versions/${VERSION}/builds/latest \
    | jq -r '.downloads["server:default"].url')

  if [ -z "${DOWNLOAD_URL}" ] || [ "${DOWNLOAD_URL}" = "null" ]; then
    echo "Paper download URL not found"
    exit 1
  fi

  echo "Downloading Paper from: ${DOWNLOAD_URL}"
  curl -L -o "${JAR_NAME}" "${DOWNLOAD_URL}"
fi

# server.properties 保証
if [ ! -f server.properties ]; then
  cat <<EOF > server.properties
level-name=world
level-type=minecraft\:flat
generate-structures=false
EOF
fi

JAVA_OPTS=${JAVA_OPTS:-"-Xms4G -Xmx4G -XX:+UseG1GC"}

exec java $JAVA_OPTS -jar "${JAR_NAME}" --nogui
