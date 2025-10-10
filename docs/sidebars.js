/**
 * Creating a sidebar enables you to:
 - create an ordered group of docs
 - render a sidebar for each doc of that group
 - provide next/previous navigation

 The sidebars can be generated from the filesystem, or explicitly defined here.

 Create as many sidebars as you want.
 */

// @ts-check

/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
  // By default, Docusaurus generates a sidebar from the docs folder structure
  tutorialSidebar: [
    {
      type: 'doc',
      id: 'getting-started',
      label: '🚀 Getting Started',
    },
    {
      type: 'doc',
      id: 'comparison',
      label: '⚖️ vs Alternatives',
    },
    {
      type: 'category',
      label: '📖 Usage Examples',
      items: [
        'usage/java-examples',
        'usage/kotlin-examples',
        'usage/json-handling',
        'usage/error-handling-examples',
      ],
    },
    {
      type: 'category',
      label: '🔧 Advanced Features',
      items: [
        'advanced-features',
        'advanced/interceptors',
      ],
    },
    {
      type: 'category',
      label: '✅ Best Practices',
      items: [
        'best-practices/production-checklist',
        'best-practices/common-mistakes',
        'best-practices/performance-tips',
      ],
    },
    {
      type: 'category',
      label: '📚 API Reference',
      items: [
        'api/overview',
        'api/api-reference',
        'api/exceptions',
      ],
    },
    {
      type: 'category',
      label: '🔄 Migration Guides',
      items: [
        'migration/from-okhttp',
        'migration-guide',
        'migration-guide-v12',
      ],
    },
    {
      type: 'doc',
      id: 'contributing',
      label: '🤝 Contributing',
    },
    {
      type: 'doc',
      id: 'license',
      label: '📄 License',
    },
  ],

  // But you can create a sidebar manually
  /*
  tutorialSidebar: [
    'intro',
    'hello',
    {
      type: 'category',
      label: 'Tutorial',
      items: ['tutorial-basics/create-a-document'],
    },
  ],
   */
};

export default sidebars;
