@echo off
setlocal
set "ROOT_DIR=%~dp0"
set "WORLD_DIR=%ROOT_DIR%darkan-world-server"
set "CLIENT_DIR=%ROOT_DIR%darkan-client"
set "GRADLE_USER_HOME=%WORLD_DIR%\.gradle-user-home"
if exist "%WORLD_DIR%\.jdk24\bin\java.exe" set "JAVA_HOME=%WORLD_DIR%\.jdk24"
if not defined JAVA_HOME (
  echo JDK 24 is required. Set JAVA_HOME before running this file.
  exit /b 1
)

cd /d "%CLIENT_DIR%"
call gradlew.bat clean shadowJar || exit /b 1
cd /d "%WORLD_DIR%"
call gradlew.bat clean shadowJar || exit /b 1
echo Build complete. Start with run.bat 25 or run.bat 50.
