import { defineConfig } from 'astro/config';
import sitemap from '@astrojs/sitemap';

export default defineConfig({
  site: 'https://didoneworld.github.io',
  base: '/identity-platform',
  integrations: [sitemap()],
  output: 'static'
});
