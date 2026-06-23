import { Router } from 'express';
import http from 'http';

const router = Router();

// API 代理到后端服务
router.use('/api', async (req, res) => {
  const backendHost = 'localhost';
  const backendPort = 8080;

  return new Promise<void>((resolve, reject) => {
    const options = {
      hostname: backendHost,
      port: backendPort,
      path: req.originalUrl,
      method: req.method,
      headers: {
        ...req.headers,
        host: `${backendHost}:${backendPort}`,
      },
    };

    const proxyReq = http.request(options, (proxyRes) => {
      // 设置状态码
      res.status(proxyRes.statusCode || 200);

      // 设置响应头
      Object.entries(proxyRes.headers).forEach(([key, value]) => {
        if (value) {
          res.setHeader(key, value);
        }
      });

      // 流式传输响应数据
      proxyRes.pipe(res);

      proxyRes.on('end', () => resolve());
    });

    proxyReq.on('error', (err) => {
      console.error('Proxy error:', err);
      if (!res.headersSent) {
        res.status(502).json({ code: 502, message: 'Backend service unavailable', data: null });
      }
      resolve();
    });

    proxyReq.on('timeout', () => {
      proxyReq.destroy();
      if (!res.headersSent) {
        res.status(504).json({ code: 504, message: 'Gateway timeout', data: null });
      }
      resolve();
    });

    // 如果有请求体，发送它
    if (req.body && Object.keys(req.body).length > 0) {
      proxyReq.write(JSON.stringify(req.body));
    }

    proxyReq.end();
  });
});

// 健康检查接口
router.get('/api/health', (req, res) => {
  res.json({
    status: 'ok',
    env: process.env.COZE_PROJECT_ENV,
    timestamp: new Date().toISOString(),
  });
});

export default router;
