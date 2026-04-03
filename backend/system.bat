@echo off

rem jarƽ��Ŀ¼
set AppName=ruoyi-admin.jar

rem JVM����
rem �Ż�˵����ѶѴ洢�� -Xms512m -Xmx1024m ���ӵ� -Xms1024m -Xmx2048m�����������
set JVM_OPTS="-Dname=%AppName%  -Duser.timezone=Asia/Shanghai -Xms512m -Xmx1024m -XX:MetaspaceSize=64m -XX:MaxMetaspaceSize=256m -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDateStamps  -XX:+PrintGCDetails -XX:NewRatio=1 -XX:SurvivorRatio=30 -XX:+UseParallelGC -XX:+UseParallelOldGC"


ECHO.
	ECHO.  [1] ����%AppName%
	ECHO.  [2] �ر�%AppName%
	ECHO.  [3] ����%AppName%
	ECHO.  [4] ����״̬ %AppName%
	ECHO.  [5] �� ��
ECHO.

ECHO.������ѡ����Ŀ�����:
set /p ID=
	IF "%id%"=="1" GOTO start
	IF "%id%"=="2" GOTO stop
	IF "%id%"=="3" GOTO restart
	IF "%id%"=="4" GOTO status
	IF "%id%"=="5" EXIT
PAUSE
:start
    for /f "usebackq tokens=1-2" %%a in (`jps -l ^| findstr %AppName%`) do (
		set pid=%%a
		set image_name=%%b
	)
	if  defined pid (
		echo %%is running
		PAUSE
	)

start javaw %JAVA_OPTS% -jar %AppName%

echo  starting����
echo  Start %AppName% success...
goto:eof

rem ����stopͨ��jps�������pid����������
:stop
	for /f "usebackq tokens=1-2" %%a in (`jps -l ^| findstr %AppName%`) do (
		set pid=%%a
		set image_name=%%b
	)
	if not defined pid (echo process %AppName% does not exists) else (
		echo prepare to kill %image_name%
		echo start kill %pid% ...
		rem ���ݽ���ID��kill����
		taskkill /f /pid %pid%
	)
goto:eof
:restart
	call :stop
    call :start
goto:eof
:status
	for /f "usebackq tokens=1-2" %%a in (`jps -l ^| findstr %AppName%`) do (
		set pid=%%a
		set image_name=%%b
	)
	if not defined pid (echo process %AppName% is dead ) else (
		echo %image_name% is running
	)
goto:eof
