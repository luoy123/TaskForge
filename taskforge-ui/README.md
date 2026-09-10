# taskforge-ui

TaskForge 前端（Vue 3 + Vite + JavaScript + Element Plus），与后端同仓、并列于仓库根目录。

**不要**把本目录写进父 `pom.xml` 的 `<modules>`。

## 本地开发

```bash
# 1. 先启动后端（端口 1234，见 application-dev.yml）
# 2. 再启动前端
cd taskforge-ui
npm install
npm run dev
```

浏览器打开 http://localhost:5173 。  
开发代理：`/dev-api` → `http://localhost:1234`（见 `vite.config.ts`）。

## 常用脚本

| 命令 | 作用 |
|------|------|
| `npm run dev` | 开发热更新 |
| `npm run build` | 生产构建 → `dist/` |
| `npm run preview` | 预览构建产物 |

## 目录约定（Sprint H 会扩展）

```text
src/
  api/          # 对接后端接口
  layout/       # 布局壳
  router/       # 路由 + 守卫
  stores/       # Pinia
  utils/        # request 等
  views/        # 页面
```
