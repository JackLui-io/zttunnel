#!/bin/bash
# ========================================
# 隧道监控系统部署脚本
# 使用方法: ./deploy.sh
# ========================================

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置
APP_NAME="scsdky-admin.jar"
BACKUP_DIR="backup"
LOG_DIR="logs"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  隧道监控系统 - 自动化部署脚本${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 1. 检查jar包是否存在
echo -e "${YELLOW}[1/6] 检查jar包...${NC}"
if [ ! -f "$APP_NAME" ]; then
    echo -e "${RED}错误: 找不到 $APP_NAME${NC}"
    echo -e "${YELLOW}请先将jar包上传到当前目录${NC}"
    exit 1
fi
echo -e "${GREEN}✓ jar包存在${NC}"
echo ""

# 2. 创建必要的目录
echo -e "${YELLOW}[2/6] 创建必要的目录...${NC}"
mkdir -p $BACKUP_DIR
mkdir -p $LOG_DIR
echo -e "${GREEN}✓ 目录创建完成${NC}"
echo ""

# 3. 备份旧版本
echo -e "${YELLOW}[3/6] 备份旧版本...${NC}"
if [ -f "$BACKUP_DIR/$APP_NAME" ]; then
    TIMESTAMP=$(date +%Y%m%d_%H%M%S)
    mv "$BACKUP_DIR/$APP_NAME" "$BACKUP_DIR/${APP_NAME}.${TIMESTAMP}"
    echo -e "${GREEN}✓ 旧版本已备份为: ${APP_NAME}.${TIMESTAMP}${NC}"
fi
cp "$APP_NAME" "$BACKUP_DIR/"
echo -e "${GREEN}✓ 当前版本已备份${NC}"
echo ""

# 4. 检查system.sh是否存在
echo -e "${YELLOW}[4/6] 检查启动脚本...${NC}"
if [ ! -f "system.sh" ]; then
    echo -e "${RED}错误: 找不到 system.sh${NC}"
    exit 1
fi
chmod +x system.sh
echo -e "${GREEN}✓ 启动脚本就绪${NC}"
echo ""

# 5. 停止旧进程
echo -e "${YELLOW}[5/6] 停止旧进程...${NC}"
./system.sh stop
sleep 2
echo -e "${GREEN}✓ 旧进程已停止${NC}"
echo ""

# 6. 启动新进程
echo -e "${YELLOW}[6/6] 启动新进程...${NC}"
./system.sh start
sleep 3
echo ""

# 7. 检查启动状态
echo -e "${YELLOW}检查启动状态...${NC}"
./system.sh status
echo ""

# 8. 显示最新日志
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}部署完成！${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${YELLOW}查看实时日志:${NC}"
echo -e "  tail -f $LOG_DIR/$APP_NAME.log"
echo ""
echo -e "${YELLOW}查看推送任务日志:${NC}"
echo -e "  tail -f $LOG_DIR/$APP_NAME.log | grep '推送任务'"
echo ""
echo -e "${YELLOW}其他命令:${NC}"
echo -e "  ./system.sh start    # 启动"
echo -e "  ./system.sh stop     # 停止"
echo -e "  ./system.sh restart  # 重启"
echo -e "  ./system.sh status   # 状态"
echo ""

# 显示最后20行日志
if [ -f "$LOG_DIR/$APP_NAME.log" ]; then
    echo -e "${YELLOW}最新日志（最后20行）:${NC}"
    echo -e "${BLUE}----------------------------------------${NC}"
    tail -n 20 "$LOG_DIR/$APP_NAME.log"
fi

