/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string
  readonly VITE_API_DOMAIN?: string
  readonly VITE_WS_URL?: string
  readonly VITE_DEV_BACKEND_PORT?: string
  readonly VITE_TUNNEL_WS_DEBUG?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
