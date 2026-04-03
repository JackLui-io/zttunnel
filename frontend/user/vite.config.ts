import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    // 开发环境代理：后端接口无统一前缀，将需代理的路径转发到 localhost:8026
    proxy: {
      '^/(login|logout|captchaImage|getInfo|getRouters|register|tunnel|device|notice|loop|analyze|report|ota|common|system|monitor|push|easyExcel|approachLamps|base)': {
        target: 'http://localhost:8026',
        changeOrigin: true,
      },
    },
  },
})
