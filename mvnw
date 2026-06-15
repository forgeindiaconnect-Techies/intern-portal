#!/bin/sh
# Maven Wrapper startup script for Unix
set -e
MAVEN_PROJECTBASEDIR=$(cd "$(dirname "$0")" && pwd)
WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Downloading Maven Wrapper..."
  mkdir -p "$MAVEN_PROJECTBASEDIR/.mvn/wrapper"
  curl -fsSL -o "$WRAPPER_JAR" https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar
fi

exec java ${MAVEN_OPTS} -classpath "$WRAPPER_JAR" "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" $WRAPPER_LAUNCHER "$@"
