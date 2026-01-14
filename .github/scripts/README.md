# TypeScript Demo with ESLint

这是一个纯 TypeScript 项目，配置了与主项目相同的 ESLint 规则。

## 安装依赖

```bash
pnpm install
```

## 使用说明

### 运行 ESLint 检查并自动修复

```bash
pnpm lint
```

### 运行 TypeScript 类型检查

```bash
pnpm type-check
```

## ESLint 配置说明

本项目使用 `@antfu/eslint-config` v6.2.0，配置特点：

- **TypeScript 支持**: 完整的 TypeScript 类型检查和规则
- **代码风格**:
  - 2 空格缩进
  - 单引号
  - 无分号
- **自定义规则**:
  - 允许使用 `console.log`（`no-console: off`）
  - 允许使用 `let`（`prefer-const: off`）
  - 空白行允许尾随空格

## 项目结构

```
ts-demo/
├── src/
│   └── index.ts          # 示例 TypeScript 文件
├── eslint.config.ts      # ESLint 配置（TypeScript 格式）
├── tsconfig.json         # TypeScript 配置
├── package.json          # 项目依赖和脚本
└── .gitignore           # Git 忽略文件
```
