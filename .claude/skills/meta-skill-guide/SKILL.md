---
name: meta-skill-guide
description: >
  Guide for creating and organizing Agent Skills in the Koish (wakame) project.
  Covers skill mechanics (directory structure, SKILL.md format, frontmatter conventions),
  and the process of researching a module/framework to produce a quality documentation skill.
  Use this skill when asked to create, add, or scaffold a new skill, write SKILL.md files,
  document a module or framework, or extract coding patterns into reusable guidance.
---

# Agent Skill 创建指南

本技能指导你在 Koish (wakame) 项目中创建 Agent Skill — 包括 skill 的格式规范（本文件）和从项目中探索提取领域知识的完整流程（`references/skill-creation-process.md`）。

---

## 1. Skill 是什么

Skill 是 `.claude/skills/<name>/SKILL.md` 下的 Markdown 文件。当 Claude Code 判断任务与 skill 的 `description` 匹配时，自动将文件内容注入上下文。

Skill 与 `CLAUDE.md` 的区别:
- **CLAUDE.md**: 全局或模块级指令，所有任务/对应目录任务都会加载
- **Skill**: 专业领域指令，仅在相关任务中按需加载

## 2. 目录结构

```
.claude/skills/<skill-name>/
├── SKILL.md          # 必须: 核心概念、快速参考、注意事项（始终自动加载）
├── references/       # 可选: 详细范式、API 参考、类型词汇表（Claude 按需 Read）
│   ├── topic-a.md
│   └── topic-b.md
├── assets/           # 可选: 完整代码示例、模板文件（Claude 按需 Read）
│   ├── example.kt
│   └── template.md
└── ...
```

**命名规范**: 全小写、连字符分隔。使用前缀分类:
- `dev-` — 开发领域知识（如 `dev-config`, `dev-invui`）
- `meta-` — 关于 skill/文档创建本身的指导
- `contrib-` — 贡献/PR 规范

**子目录约定**:
- `SKILL.md` 保持精简（<150 行），放核心概念和 API 快速参考
- `references/` 放详细范式、API 参考等按需查阅的内容
- `assets/` 放代码示例、模板等可独立读取的文件
- SKILL.md 末尾应有子文件索引，列出所有 references 和 assets 文件及其内容说明

## 3. SKILL.md 格式

YAML frontmatter（必须）+ Markdown 正文。

```yaml
---
name: <skill-name>        # 必须: 与目录名一致
description: >            # 必须: 功能概括 + "Use this skill when..." + 关键术语
  Guide for ...
  Use this skill when ...
---
```

**description 要点**:
- 第一句话概括功能
- 第二句话用 "Use this skill when..." 明确触发场景
- 包含相关技术术语，确保匹配准确

**正文规范**:
- 语言: 中文（与项目注释一致）
- frontmatter: 英文
- 代码块标注语言: ` ```kotlin `
- 文件末尾: 恰好一个换行符
- 禁用 ASCII art / box-drawing 字符画
- 表格用最小格式

## 4. 何时创建 Skill vs 更新 CLAUDE.md

| 场景 | 做法 |
|---|---|
| 某领域需要专业指令 (>50 行) | 创建 Skill |
| 简短的全局规则 (编码风格、命名约定) | 添加到根目录 `CLAUDE.md` |
| 模块级编码范式 | 添加到对应子目录 `CLAUDE.md` |
| 已有 Skill 覆盖的领域需补充 | 更新现有 SKILL.md 或对应 references 文件 |

## 5. 验证清单

- [ ] 目录名全小写、带前缀、连字符分隔
- [ ] 文件名为 `SKILL.md`
- [ ] frontmatter 含 `name` 和 `description`，`name` 与目录名一致
- [ ] `description` 含 "Use this skill when..."
- [ ] 正文中文，代码块标注语言
- [ ] 文件以恰好一个换行符结尾
- [ ] 若内容超过 150 行，考虑将详细内容拆分到 `references/` 或 `assets/`
- [ ] SKILL.md 末尾有子文件索引

## 6. 技能列表

当前项目已有的 Skill:

| 目录 | 用途 |
|---|---|
| `dev-config/` | 配置文件读取与编写 (Configurate + lazyconfig) |
| `dev-enchantments/` | 附魔框架架构与开发指南 |
| `dev-invui/` | 使用 InvUI2 创建 GUI 菜单 |
| `contrib-mythicmobs/` | MythicMobs Hook 开发与 PR 规范 |
| `meta-skill-guide/` | 创建 Agent Skill 的指南（本技能） |

---

## 子文件索引

- `references/skill-creation-process.md` — 创建文档型 Skill 的完整流程（应含内容、探索流程、章节大纲、容量控制）
