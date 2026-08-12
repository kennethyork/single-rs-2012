#!/bin/bash
JAVA_VERSION=$(java --version | grep Temurin)
if [[ ${JAVA_VERSION} != *"Temurin"* ]];then
  echo "You need to be using a Temurin Java build."
  exit
fi

# Build the Java bundled jar
./gradlew shadowJar

rm -rf ./build/jdk19
# JLink tem package with minimal files
jlink --no-header-files --no-man-pages --compress=2 --strip-debug --vm=server --exclude-files="**/bin/rmiregistry,**/bin/jrunscript,**/bin/rmid" --add-modules java.base,java.instrument,java.logging,java.management,java.naming,java.scripting,java.se,java.security.jgss,java.security.sasl,java.sql,java.transaction.xa,java.xml,java.xml.crypto,jdk.security.auth,jdk.xml.dom,jdk.unsupported,jdk.crypto.cryptoki,jdk.crypto.ec --output ./build/jdk19

# Package the appimage base folder structure
jpackage --main-jar darkan-loader-shaded-1.0.1-all.jar --runtime-image ./build/jdk19 --java-options "-Djava.library.path=../lib/runtime/lib" --name Darkan --input ./build/libs --dest ./build/appimage --type app-image

# Copy the appimage logo files, desktop, and app run script
cp ./appimage/* ./build/appimage/Darkan/

# Build the appimage
./appimagetool-x86_64.AppImage ./build/appimage/Darkan ./build/appimage/Darkan.AppImage