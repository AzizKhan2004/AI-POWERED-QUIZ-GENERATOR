#!/bin/sh

#
# Copyright 2015 the original authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -e

APP_BASE_NAME=${0##*/}
APP_HOME=$(cd "${0%/*}" && pwd -P)

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

JAVA_OPTS=${JAVA_OPTS:-"-Xmx64m"}

# Locate java binary
if [ -n "$JAVA_HOME" ] ; then
  JAVA_BIN="$JAVA_HOME/bin/java"
else
  JAVA_BIN="java"
fi

if ! command -v "$JAVA_BIN" >/dev/null 2>&1 ; then
  echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2
  echo "Please set JAVA_HOME to the location of your JDK." >&2
  exit 1
fi

exec "$JAVA_BIN" \
  $JAVA_OPTS \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"

