// src/main/frontend/theme.ts
export function applyTheme(html: string): string {
  // Simple example: injects a comment
  return html.replace('</head>', '<!-- Theme applied --></head>');
}