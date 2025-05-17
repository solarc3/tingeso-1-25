import path from "path"
import tailwindcss from "@tailwindcss/vite"
import react from "@vitejs/plugin-react"
import { defineConfig } from "vite"

export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
    server: {
      host: true,            // equivale a 0.0.0.0
      port: 5173,
      // ➊ Lo importante:
      allowedHosts: ['gurt.solarworks.cl']   // o true para aceptar cualquiera
      // Si accedes por otro nombre (p.e. “frontend_web” en Docker),
      // añádelo también: ['gurt.solarworks.cl', 'frontend_web']
    },
  },
})
