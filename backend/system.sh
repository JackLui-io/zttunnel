#!/bin/bash
# ./system.sh start 启动 stop 停止 restart 重启 status 状态
AppName=scsdky-admin.jar

# JVM参数
# 优化说明：服务器总内存2GB，JVM堆内存设置为512m-1024m，为系统和其他进程预留约1GB内存
# 元空间设置为64m-256m，减少内存占用
JVM_OPTS="-Dname=$AppName  -Duser.timezone=Asia/Shanghai -Xms512m -Xmx1024m -XX:MetaspaceSize=64m -XX:MaxMetaspaceSize=256m -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDateStamps  -XX:+PrintGCDetails -XX:NewRatio=1 -XX:SurvivorRatio=30 -XX:+UseParallelGC -XX:+UseParallelOldGC"
APP_HOME=`pwd`
LOG_PATH=$APP_HOME/logs/$AppName.log

if [ "$1" = "" ];
then
    echo -e "\033[0;31m 未输入操作名 \033[0m  \033[0;34m {start|stop|restart|status|log} \033[0m"
    exit 1
fi

if [ "$AppName" = "" ];
then
    echo -e "\033[0;31m 未输入应用名 \033[0m"
    exit 1
fi

start()
{
    PID=`ps -ef |grep java|grep $AppName|grep -v grep|awk '{print $2}'`

	if [ x"$PID" != x"" ]; then
	    echo "$AppName is running..."
	    echo "PID: $PID"
	else
		# 确保logs目录存在
		mkdir -p logs
		
		echo "Starting $AppName..."
		
		# 后台启动应用，确保不阻塞当前shell
		nohup java $JVM_OPTS -jar $AppName >> $LOG_PATH 2>&1 &
		
		# 获取启动的进程ID
		NEW_PID=$!
		
		# 等待2秒，检查进程是否还在运行
		sleep 2
		
		if ps -p $NEW_PID > /dev/null 2>&1; then
		    echo "✅ $AppName started successfully"
		    echo "PID: $NEW_PID"
		    echo "日志文件: $LOG_PATH"
		    echo ""
		    echo "查看实时日志: ./system.sh log"
		    echo "查看状态: ./system.sh status"
		    echo ""
		    echo "提示: 应用正在后台启动，完整启动可能需要10-30秒"
		    echo "      请稍后使用以下命令检查启动状态："
		    echo "      tail -f $LOG_PATH"
		else
		    echo "❌ $AppName failed to start"
		    echo "请查看日志: tail -n 50 $LOG_PATH"
		    exit 1
		fi
	fi
}

stop()
{
    echo "Stop $AppName"

	PID=""
	query(){
		PID=`ps -ef |grep java|grep $AppName|grep -v grep|awk '{print $2}'`
	}

	query
	if [ x"$PID" != x"" ]; then
		kill -TERM $PID
		echo "$AppName (pid:$PID) exiting..."
		while [ x"$PID" != x"" ]
		do
			sleep 1
			query
		done
		echo "$AppName exited."
	else
		echo "$AppName already stopped."
	fi
}

restart()
{
    stop
    sleep 2
    start
}

status()
{
    PID=`ps -ef |grep java|grep $AppName|grep -v grep|wc -l`
    if [ $PID != 0 ];then
        echo "$AppName is running..."
        echo "日志文件: $LOG_PATH"
    else
        echo "$AppName is not running..."
    fi
}

log()
{
    if [ ! -f "$LOG_PATH" ]; then
        echo "日志文件不存在: $LOG_PATH"
        exit 1
    fi
    
    echo "正在查看日志文件: $LOG_PATH"
    echo "按 Ctrl+C 退出"
    echo "----------------------------------------"
    tail -f $LOG_PATH
}

case $1 in
    start)
    start;;
    stop)
    stop;;
    restart)
    restart;;
    status)
    status;;
    log)
    log;;
    *)
    echo -e "\033[0;31m 无效的操作名 \033[0m"
    echo -e "\033[0;34m 使用方法: $0 {start|stop|restart|status|log} \033[0m"
    exit 1
    ;;
esac
