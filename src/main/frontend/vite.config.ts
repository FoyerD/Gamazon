import { defineConfig } from 'vite';
import { applyTheme } from './theme';
import path from 'path';

export default defineConfig({
  root: path.resolve(__dirname),
  build: {
    outDir: path.resolve(__dirname, 'dist'),
    rollupOptions: {
      input: path.resolve(__dirname, 'index.html'), // 🟢 Declare index.html as entry
    },
  },
  plugins: [
    {
      name: 'apply-theme',
      transformIndexHtml(html) {
        return applyTheme(html);
      },
    },
  ],
  resolve: {
    alias: {
      frontend: path.resolve(__dirname),
    },
  },
});
