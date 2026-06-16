import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Em desenvolvimento, /api é redirecionado para o backend Spring Boot (porta 8080).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'react-vendor': ['react', 'react-dom', 'react-router-dom'],
          'query-vendor': ['@tanstack/react-query', 'axios'],
          charts: ['recharts'],
          icons: ['lucide-react'],
        },
      },
    },
  },
})
