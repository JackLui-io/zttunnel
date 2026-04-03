@echo off
setlocal

set "REDIS_PROCESS=redis-server.exe"

tasklist /FI "IMAGENAME eq %REDIS_PROCESS%" | find /I "%REDIS_PROCESS%" >nul
if not "%ERRORLEVEL%"=="0" (
  echo [INFO] Redis is not running.
  exit /b 0
)

echo [INFO] Stopping Redis...
taskkill /F /IM "%REDIS_PROCESS%" >nul 2>&1
if not "%ERRORLEVEL%"=="0" (
  echo [ERROR] Failed to stop Redis process: %REDIS_PROCESS%
  exit /b 1
)

timeout /t 1 /nobreak >nul
tasklist /FI "IMAGENAME eq %REDIS_PROCESS%" | find /I "%REDIS_PROCESS%" >nul
if not "%ERRORLEVEL%"=="0" (
  echo [OK] Redis stopped successfully.
  exit /b 0
)

echo [WARN] Stop command executed, but Redis process is still running.
exit /b 2

