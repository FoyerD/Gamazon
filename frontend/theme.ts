export function applyTheme(html: string): string {
  return html.replace('</head>', '<!-- Theme applied -->\n</head>');
}
