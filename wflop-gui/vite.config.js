export default {
  optimizeDeps: {
    include: ['react', 'react-dom', 'pixi.js', '@pixi/react']
  },
  define: {
    global: 'globalThis',
  }
}