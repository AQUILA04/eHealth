import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  preview: {
    allowedHosts: 'all',
  },
  server: {
    port: 5173,
    proxy: {
      '/api/v1/gap': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/api/v1/dpi': {
        target: 'http://localhost:8083',
        changeOrigin: true,
      },
      '/api/v1/empi': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
    },
  },
})
