#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PID_FILE="$ROOT_DIR/logs/taskforge.pid"
PORT=1234

if [[ ! -f "$PID_FILE" ]]; then
  echo "未找到 pid 文件: $PID_FILE（可能未通过 start.sh 启动）"
  exit 1
fi

pid="$(cat "$PID_FILE" 2>/dev/null || true)"
if [[ -z "$pid" ]]; then
  rm -f "$PID_FILE"
  echo "pid 文件为空，已清理。"
  exit 0
fi

if ! kill -0 "$pid" 2>/dev/null; then
  rm -f "$PID_FILE"
  echo "进程 $pid 已不存在，已清理 pid 文件。"
  exit 0
fi

echo "停止 TaskForge (pid=$pid)..."
kill "$pid" 2>/dev/null || true
for _ in $(seq 1 20); do
  kill -0 "$pid" 2>/dev/null || break
  sleep 0.5
done
if kill -0 "$pid" 2>/dev/null; then
  echo "正常停止超时，强制结束..."
  kill -9 "$pid" 2>/dev/null || true
fi
rm -f "$PID_FILE"
echo "已停止。"
