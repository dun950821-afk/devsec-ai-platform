#!/bin/bash
set -Eeuo pipefail

COZE_WORKSPACE_PATH="${COZE_WORKSPACE_PATH:-$(pwd)}"

cd "${COZE_WORKSPACE_PATH}"

echo "Installing dependencies..."
pnpm install --prefer-frozen-lockfile --prefer-offline --loglevel warn --reporter=silent

echo "Building frontend with Vite..."
pnpm vite build

# 构建服务器（生产模式，使用 ESM 格式）
echo "Bundling server with tsup..."
COZE_PROJECT_ENV=PROD pnpm tsup server/server.ts --format esm --platform node --target node20 --outDir dist-server --no-splitting --no-minify --external vite --external express

echo "Build completed successfully!"
