#!/bin/sh
# Gradle wrapper 启动脚本（Unix）
APP_HOME=$(cd "${0%/*}" && pwd -P)/..
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

if [ -n "$JAVA_HOME" ] ; then
    JAVACMD=$JAVA_HOME/bin/java
else
    JAVACMD=java
fi

exec "$JAVACMD" "-Xmx64m" "-Xms64m" \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain "$@"
