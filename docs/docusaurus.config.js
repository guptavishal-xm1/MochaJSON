import {themes as prismThemes} from 'prism-react-renderer';

/**
 * SEO-OPTIMIZED DOCUSAURUS CONFIGURATION FOR MOCHAJSON
 * 
 * This configuration enhances:
 * 1. Google/Bing SEO indexing with comprehensive metadata
 * 2. AI discoverability (OpenAI, Gemini, Anthropic) through structured data
 * 3. Social media sharing (Twitter, LinkedIn, Slack) with Open Graph tags
 * 4. Search engine crawling with sitemap and robots.txt
 * 5. Brand consistency and canonical URL management
 * 
 * Key SEO improvements:
 * - Enhanced metadata for better search ranking
 * - Social sharing optimization for viral potential
 * - Structured data for AI model training
 * - Canonical URLs to prevent duplicate content issues
 * - Comprehensive sitemap for search engine discovery
 */

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'MochaJSON — Unified HTTP + JSON Client for Java & Kotlin',
  tagline: 'Lightweight, fluent, and modern HTTP + JSON library for developers',
  favicon: 'img/favicon.ico',

  // Production URL configuration for GitHub Pages
  url: 'https://guptavishal-xm1.github.io',
  baseUrl: '/MochaJSON/',

  // GitHub Pages deployment configuration
  organizationName: 'guptavishal-xm1',
  projectName: 'MochaJSON',

  // SEO and link management
  onBrokenLinks: 'throw',
  markdown: {
    hooks: {
      onBrokenMarkdownLinks: 'warn',
    },
  },
  
  // Internationalization (English only for now)
  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  // Documentation configuration
  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          path: './docs',
          routeBasePath: '/',
          sidebarPath: './sidebars.js',
          editUrl: 'https://github.com/guptavishal-xm1/MochaJSON/tree/main/docs/',
          // Exclude patterns for clean build
          exclude: [
            '**/node_modules/**',
            '**/build/**',
            '**/dist/**',
            '**/.git/**',
            '**/package*.json',
            '**/docusaurus.config.js',
            '**/sidebars.js',
            '**/src/**',
            '**/static/**',
            '**/components/**',
          ],
        },
        blog: false,
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      }),
    ],
  ],

  // Theme configuration with comprehensive SEO metadata
  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      // Social sharing image
      image: 'img/social-card.jpg',
      
      // Comprehensive SEO metadata
      metadata: [
        // Basic SEO meta tags
        { name: 'keywords', content: 'MochaJSON, Java HTTP client, Kotlin JSON parser, OkHttp alternative, Gson alternative, fluent API, REST client Java, async HTTP, virtual threads Java 21, connection pooling, retry mechanism, circuit breaker, HTTP caching, file operations' },
        { name: 'description', content: 'MochaJSON is a unified HTTP and JSON library for Java and Kotlin — fast, fluent, and lightweight for modern API integrations. Replace OkHttp + Gson with one dependency.' },
        { name: 'author', content: 'Vishal Gupta' },
        { name: 'robots', content: 'index, follow' },
        { name: 'googlebot', content: 'index, follow' },
        
        // Open Graph tags for social sharing
        { property: 'og:title', content: 'MochaJSON — Unified HTTP + JSON Client for Java & Kotlin' },
        { property: 'og:description', content: 'Lightweight fluent Java & Kotlin library for handling HTTP and JSON effortlessly. Perfect alternative to OkHttp + Gson with v1.2.0 features.' },
        { property: 'og:type', content: 'website' },
        { property: 'og:url', content: 'https://guptavishal-xm1.github.io/MochaJSON/' },
        { property: 'og:image', content: 'https://guptavishal-xm1.github.io/MochaJSON/img/social-card.jpg' },
        { property: 'og:image:width', content: '1280' },
        { property: 'og:image:height', content: '640' },
        { property: 'og:site_name', content: 'MochaJSON Documentation' },
        { property: 'og:locale', content: 'en_US' },
        
        // Twitter Card tags
        { property: 'twitter:card', content: 'summary_large_image' },
        { property: 'twitter:title', content: 'MochaJSON — Unified HTTP + JSON Client' },
        { property: 'twitter:description', content: 'Simplify your Java and Kotlin API development with MochaJSON v1.2.0. One library to replace OkHttp + Gson with advanced features.' },
        { property: 'twitter:image', content: 'https://guptavishal-xm1.github.io/MochaJSON/img/social-card.jpg' },
        { property: 'twitter:creator', content: '@guptavishal_xm1' },
        { property: 'twitter:site', content: '@guptavishal_xm1' },
        
        // Additional SEO meta tags
        { name: 'theme-color', content: '#7c3aed' },
        { name: 'msapplication-TileColor', content: '#7c3aed' },
        { name: 'apple-mobile-web-app-capable', content: 'yes' },
        { name: 'apple-mobile-web-app-status-bar-style', content: 'default' },
        { name: 'apple-mobile-web-app-title', content: 'MochaJSON' },
      ],

      // Navigation bar
      navbar: {
        title: 'MochaJSON',
        logo: {
          alt: 'MochaJSON Logo',
          src: 'img/logo.svg',
        },
        items: [
          {
            type: 'docSidebar',
            sidebarId: 'tutorialSidebar',
            position: 'left',
            label: 'Documentation',
          },
          {
            href: 'https://search.maven.org/artifact/io.github.guptavishal-xm1/MochaJSON/1.2.0/jar',
            label: 'Maven Central',
            position: 'right',
          },
          {
            href: 'https://github.com/guptavishal-xm1/MochaJSON',
            label: 'GitHub',
            position: 'right',
          },
        ],
      },

      // Footer with enhanced links
      footer: {
        style: 'dark',
        links: [
          {
            title: 'Documentation',
            items: [
              {
                label: 'Getting Started',
                to: '/MochaJSON/getting-started',
              },
              {
                label: 'Java Examples',
                to: '/MochaJSON/usage/java-examples',
              },
              {
                label: 'Kotlin Examples',
                to: '/MochaJSON/usage/kotlin-examples',
              },
              {
                label: 'API Reference',
                to: '/MochaJSON/api/overview',
              },
            ],
          },
          {
            title: 'Resources',
            items: [
              {
                label: 'Maven Central',
                href: 'https://search.maven.org/artifact/io.github.guptavishal-xm1/MochaJSON/1.2.0/jar',
              },
              {
                label: 'JavaDocs',
                href: 'https://javadoc.io/doc/io.github.guptavishal-xm1/MochaJSON/latest/index.html',
              },
              {
                label: 'GitHub Repository',
                href: 'https://github.com/guptavishal-xm1/MochaJSON',
              },
              {
                label: 'Issues & Bug Reports',
                href: 'https://github.com/guptavishal-xm1/MochaJSON/issues',
              },
            ],
          },
          {
            title: 'Community',
            items: [
              {
                label: 'GitHub Discussions',
                href: 'https://github.com/guptavishal-xm1/MochaJSON/discussions',
              },
              {
                label: 'Contributing Guide',
                to: '/MochaJSON/contributing',
              },
              {
                label: 'License',
                to: '/MochaJSON/license',
              },
              {
                label: 'Security Policy',
                href: 'https://github.com/guptavishal-xm1/MochaJSON/security',
              },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} MochaJSON. Built with Docusaurus. MIT Licensed.`,
      },

      // Code highlighting
      prism: {
        theme: prismThemes.github,
        darkTheme: prismThemes.dracula,
        additionalLanguages: ['java', 'kotlin', 'gradle'],
      },

      // Color mode configuration
      colorMode: {
        defaultMode: 'light',
        disableSwitch: false,
        respectPrefersColorScheme: true,
      },

      // Search configuration (Algolia - disabled until proper setup)
      // TODO: Configure Algolia search when API keys are available
      // algolia: {
      //   appId: 'YOUR_APP_ID',
      //   apiKey: 'YOUR_SEARCH_API_KEY',
      //   indexName: 'mochajson-docs',
      //   contextualSearch: true,
      //   searchParameters: {},
      //   searchPagePath: 'search',
      // },
    }),

  // Plugins for enhanced functionality
  plugins: [
    // Image optimization
    [
      '@docusaurus/plugin-ideal-image',
      {
        quality: 70,
        max: 1030,
        min: 640,
        steps: 2,
        disableInDev: false,
      },
    ],

    // PWA support (optional - uncomment if needed)
    // [
    //   '@docusaurus/plugin-pwa',
    //   {
    //     debug: false,
    //     offlineModeActivationStrategies: ['appInstalled', 'standalone', 'queryString'],
    //     pwaHead: [
    //       { tagName: 'link', rel: 'icon', href: '/img/favicon.ico' },
    //       { tagName: 'link', rel: 'manifest', href: '/manifest.json' },
    //       { tagName: 'meta', name: 'theme-color', content: '#7c3aed' },
    //     ],
    //   },
    // ],
  ],

  // Custom scripts for analytics (add your tracking codes here)
  scripts: [
    // {
    //   src: 'https://www.googletagmanager.com/gtag/js?id=GA_TRACKING_ID',
    //   async: true,
    // },
  ],

  // Custom CSS
  stylesheets: [
    // Add any additional stylesheets here
  ],
};

export default config;
