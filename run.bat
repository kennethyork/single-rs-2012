@echo off
setlocal
set "ROOT_DIR=%~dp0"
set "WORLD_DIR=%ROOT_DIR%darkan-world-server"
set "WORLD_JAR=%WORLD_DIR%\build\libs\world-server-2.0.12-all.jar"
set "CLIENT_JAR=%ROOT_DIR%darkan-client\build\libs\darkan-client-shaded-1.0.1-all.jar"
set "CACHE_DIR=%ROOT_DIR%darkan-cache"
set "SAVE_DIR=%ROOT_DIR%saves"
set "XP_RATE=%~1"
if "%XP_RATE%"=="" set "XP_RATE=25"
if not "%XP_RATE%"=="25" if not "%XP_RATE%"=="50" (
  echo Usage: run.bat [25^|50]
  exit /b 2
)

set "JAVA_BIN=java"
if exist "%WORLD_DIR%\.jdk24\bin\java.exe" set "JAVA_BIN=%WORLD_DIR%\.jdk24\bin\java.exe"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_BIN=%JAVA_HOME%\bin\java.exe"

if not exist "%WORLD_JAR%" (
  echo Offline game jars are missing. Run build-offline.bat first.
  exit /b 1
)
if not exist "%CLIENT_JAR%" (
  echo Offline game jars are missing. Run build-offline.bat first.
  exit /b 1
)
if not exist "%CACHE_DIR%\main_file_cache.dat2" (
  echo The Darkan cache is missing from "%CACHE_DIR%".
  exit /b 1
)

if not exist "%SAVE_DIR%" mkdir "%SAVE_DIR%"
cd /d "%WORLD_DIR%"
"%JAVA_BIN%" -Xmx3g -Ddarkan.singlePlayer=true -Ddarkan.cache.path="%CACHE_DIR%" -Ddarkan.save.path="%SAVE_DIR%" -Ddarkan.xpRate=%XP_RATE% -cp "%WORLD_JAR%;%CLIENT_JAR%" com.rs.SinglePlayerLauncher
