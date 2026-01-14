import antfu from '@antfu/eslint-config'

export default antfu({
  // 启用 TypeScript 支持
  typescript: true,

  // 自定义样式规则
  stylistic: {
    indent: 2, // 2 空格缩进
    quotes: 'single', // 单引号
    semi: false, // 无分号
  },

  // 忽略的文件/目录
  ignores: [
    '**/dist/**',
    '**/node_modules/**',
    '**/coverage/**',
    '*.md',
  ],

  rules: {
    'no-console': 'off',
    'prefer-const': 'off',
    'style/no-trailing-spaces': ['error', { skipBlankLines: true }],
  },
})
