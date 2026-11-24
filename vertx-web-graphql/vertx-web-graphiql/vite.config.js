import {defineConfig} from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(() => {
  return {
    base: '',
    build: {
      outDir: 'build',
    },
    plugins: [
      react(),
    ],
    worker: {
      format: 'es',
    },
    optimizeDeps: {
      exclude: [
        'graphiql/setup-workers/vite',
        '@graphiql/react/setup-workers/vite',
      ],
    },
  };
});
