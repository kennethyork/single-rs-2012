#!/bin/bash
# Build the Java bundled jar
JAVA_HOME=./osxjdk/Contents/Home ./gradlew shadowJar

rm -rf ./build/jdk19
Rm -rf ./build/dmg
# JLink tem package with minimal files
./osxjdk/Contents/Home/bin/jlink --add-modules ALL-MODULE-PATH --output ./build/jdk19

# Package the appimage base folder structure
./osxjdk/Contents/Home/bin/jpackage --main-jar darkan-loader-shaded-1.0.1-all.jar --runtime-image ./build/jdk19 --java-options "-Djava.library.path=../lib/runtime/lib" --name Darkan --input ./build/libs --dest ./build/dmg --type dmg --icon ./darkan.icns