#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

MODULE="taskforge-admin"
JAR="$ROOT_DIR/$MODULE/target/taskforge-admin-0.0.1-SNAPSHOT.jar"
PID_FILE="$ROOT_DIR/logs/taskforge.pid"
LOG_FILE="$ROOT_DIR/logs/taskforge.log"
PORT=1234
SKIP_BUILD=0
FOREGROUND=0

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --skip-build   不重新编译，直接启动已有 jar
  --fg           前台运行（默认后台）
  -h, --help     显示帮助
USAGE
}

for arg in "$@"; do
  case "$arg" in
    --skip-build) SKIP_BUILD=1 ;;
    --fg) FOREGROUND=1 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "未知参数: $arg"; usage; exit 1 ;;
  esac
done

if [[ -f "$PID_FILE" ]]; then
  old_pid="$(cat "$PID_FILE" 2>/dev/null || true)"
  if [[ -n "${old_pid}" ]] && kill -0 "$old_pid" 2>/dev/null; then
    echo "检测到已在运行 (pid=$old_pid)，先停止..."
    kill "$old_pid" 2>/dev/null || true
    for _ in $(seq 1 20); do
      kill -0 "$old_pid" 2>/dev/null || break
      sleep 0.5
    done
    if kill -0 "$old_pid" 2>/dev/null; then
      kill -9 "$old_pid" 2>/dev/null || true
    fi
  fi
  rm -f "$PID_FILE"
fi

if command -v ss >/dev/null 2>&1; then
  occupied="$(ss -tlnp 2>/dev/null | grep -E ":${PORT}\\b" || true)"
  if [[ -n "$occupied" ]]; then
    echo "端口 ${PORT} 仍被占用，请先释放后再启动："
    echo "$occupied"
    exit 1
  fi
fi

if [[ "$SKIP_BUILD" -eq 0 ]]; then
  echo "==> Maven package (skip tests)..."
  mvn -q -pl "$MODULE" -am package -DskipTests
else
  if [[ ! -f "$JAR" ]]; then
    echo "找不到 jar: $JAR"
    echo "请去掉 --skip-build 先编译一次。"
    exit 1
  fi
  echo "==> 跳过编译，使用现有 jar"
fi

mkdir -p "$ROOT_DIR/logs"

if [[ "$FOREGROUND" -eq 1 ]]; then
  echo "==> 前台启动: $JAR"
  echo "    端口: http://127.0.0.1:${PORT}"
  exec java -jar "$JAR"
fi

echo "==> 后台启动: $JAR"
nohup java -jar "$JAR" >>"$LOG_FILE" 2>&1 &
echo $! >"$PID_FILE"
echo "    pid=$(cat "$PID_FILE")"
echo "    端口: http://127.0.0.1:${PORT}"
echo "    日志: $LOG_FILE"
echo "    停止: scripts/stop.sh"
