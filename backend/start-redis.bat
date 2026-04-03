@echo off
setlocal

set "REDIS_EXE=C:\Program Files\Redis8\redis-server.exe"
set "REDIS_PROCESS=redis-server.exe"

if not exist "%REDIS_EXE%" (
  echo [ERROR] Redis executable not found: "%REDIS_EXE%"
  echo Please verify your Redis install path.
  exit /b 1
)

tasklist /FI "IMAGENAME eq %REDIS_PROCESS%" | find /I "%REDIS_PROCESS%" >nul
if "%ERRORLEVEL%"=="0" (
  echo [INFO] Redis is already running.
  exit /b 0
)

echo [INFO] Starting Redis in current terminal (no new cmd window)...
start "" /B "%REDIS_EXE%" >nul 2>&1
if not "%ERRORLEVEL%"=="0" (
  echo [ERROR] Failed to execute Redis startup command.
  exit /b 1
)

timeout /t 2 /nobreak >nul
tasklist /FI "IMAGENAME eq %REDIS_PROCESS%" | find /I "%REDIS_PROCESS%" >nul
if "%ERRORLEVEL%"=="0" (
  echo [OK] Redis started successfully.
  exit /b 0
)

echo [WARN] Redis start command executed, but process was not detected.
echo Check startup logs or run "%REDIS_EXE%" manually for details.
exit /b 2
