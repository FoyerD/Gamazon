// src/main/frontend/vite.config.ts
import { applyTheme } from './theme';

export default {
  plugins: [
    {
      name: 'apply-theme',
      transformIndexHtml(html) {
        return applyTheme(html);
      }
    }
  ]
};
