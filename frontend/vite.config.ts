import { applyTheme } from './theme';

export default {
  plugins: [
    {
      name: 'apply-theme',
      transformIndexHtml(html: string) {
        return applyTheme(html);
      }
    }
  ]
};