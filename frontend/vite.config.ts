import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { VitePWA } from 'vite-plugin-pwa'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
    VitePWA({
      registerType: 'autoUpdate',
      workbox: {
        // 새 서비스워커가 활성화되면 이전 버전이 캐시해둔 옛 해시 자산을 즉시 정리한다.
        cleanupOutdatedCaches: true,
        // 클라이언트 라우트(/wardrobe 등)로의 이동을 index.html로 폴백시켜, SW가 활성화된
        // 상태에서도 새로고침/딥링크가 항상 최신 프리캐시 shell을 받도록 한다.
        navigateFallback: '/index.html',
      },
      manifest: {
        name: 'todaystyle — 오늘의 코디',
        short_name: 'todaystyle',
        description: '매일의 OOTD를 기록하고, 안 입어본 조합을 추천받는 코디 서비스',
        theme_color: '#111827',
        background_color: '#ffffff',
        display: 'standalone',
        start_url: '/',
        icons: [
          { src: 'pwa-192.png', sizes: '192x192', type: 'image/png' },
          { src: 'pwa-512.png', sizes: '512x512', type: 'image/png' },
        ],
      },
    }),
  ],
  server: {
    // 개발 중 CORS 회피: /api 요청을 스프링 부트(8080)로 프록시한다.
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
