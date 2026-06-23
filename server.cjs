const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = process.env.DEPLOY_RUN_PORT || 5000;
const BACKEND = 'http://localhost:8080';

const MIME_TYPES = {
  '.html': 'text/html',
  '.js': 'application/javascript',
  '.css': 'text/css',
  '.json': 'application/json'
};

http.createServer((req, res) => {
  if (req.url.startsWith('/api/')) {
    const options = {
      hostname: 'localhost',
      port: 8080,
      path: req.url,
      method: req.method,
      headers: { 'Content-Type': 'application/json' }
    };
    const proxyReq = http.request(options, (proxyRes) => {
      let data = '';
      proxyRes.on('data', chunk => data += chunk);
      proxyRes.on('end', () => {
        res.writeHead(proxyRes.statusCode, { 'Content-Type': 'application/json' });
        res.end(data);
      });
    });
    req.pipe(proxyReq);
    return;
  }

  let filePath = req.url === '/' ? '/index.html' : req.url;
  filePath = path.join(__dirname, filePath);
  const ext = path.extname(filePath);
  
  fs.readFile(filePath, (err, data) => {
    if (err) {
      res.writeHead(404);
      res.end('Not Found');
      return;
    }
    res.writeHead(200, { 'Content-Type': MIME_TYPES[ext] || 'text/plain' });
    res.end(data);
  });
}).listen(PORT, '0.0.0.0', () => console.log('Running on ' + PORT));
