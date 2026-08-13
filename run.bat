@echo off
setlocal
set "ROOT_DIR=%~dp0"
set "SAVE_DIR=%ROOT_DIR%saves"
set "XP_RATE=%~1"

if exist "%ROOT_DIR%world\world-server.jar" if exist "%ROOT_DIR%client.jar" (
  set "WORLD_DIR=%ROOT_DIR%world"
  set "WORLD_JAR=%ROOT_DIR%world\world-server.jar"
  set "CLIENT_JAR=%ROOT_DIR%client.jar"
  set "CACHE_DIR=%ROOT_DIR%cache"
  set "JAVA_BIN=%ROOT_DIR%runtime\bin\java.exe"
) else (
  set "WORLD_DIR=%ROOT_DIR%darkan-world-server"
  set "WORLD_JAR=%ROOT_DIR%darkan-world-server\build\libs\world-server-2.0.12-all.jar"
  set "CLIENT_JAR=%ROOT_DIR%darkan-client\build\libs\darkan-client-shaded-1.0.1-all.jar"
  set "CACHE_DIR=%ROOT_DIR%darkan-cache"
  set "JAVA_BIN=java"
  if exist "%ROOT_DIR%darkan-world-server\.jdk24\bin\java.exe" set "JAVA_BIN=%ROOT_DIR%darkan-world-server\.jdk24\bin\java.exe"
  if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_BIN=%JAVA_HOME%\bin\java.exe"
)
if "%XP_RATE%"=="" set "XP_RATE=1"
set "INVALID_RATE="
for /f "delims=0123456789" %%A in ("%XP_RATE%") do set "INVALID_RATE=1"
if defined INVALID_RATE goto invalid_rate
if "%XP_RATE%"=="0" goto invalid_rate
goto valid_rate

:invalid_rate
  echo Usage: run.bat [xp rate]   e.g. run.bat ^(1x^), run.bat 25, run.bat 50
  exit /b 2

:valid_rate

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
