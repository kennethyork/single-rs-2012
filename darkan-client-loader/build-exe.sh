#!/bin/bash
JAVA_HOME=./winjdk
GRADLE_JAVA_HOME=./windjdk
# Build the Java bundled jar
./gradlew shadowJar

rm -rf ./build/jdk19
# JLink tem package with minimal files
./winjdk/bin/jlink --add-modules ALL-MODULE-PATH --output ./build/jdk19

# Package the appimage base folder structure
./winjdk/bin/jpackage --main-jar darkan-loader-shaded-1.0.1-all.jar --runtime-image ./build/jdk19 --java-options "-Djava.library.path=../lib/runtime/lib" --name Darkan --input ./build/libs --dest ./build/exe --type app-image --icon ./darkan.ico