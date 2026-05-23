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

本技能指导你在 Koish (wakame) 项目中创建 Agent Skill — 包括 skill 的格式规范（第一部分）和从项目中探索提取领域知识的完整流程（第二部分）。

---

## 第一部分: Skill 机制

### 1. Skill 是什么

Skill 是 `.claude/skills/<name>/SKILL.md` 下的 Markdown 文件。当 Claude Code 判断任务与 skill 的 `description` 匹配时，自动将文件内容注入上下文。

Skill 与 `CLAUDE.md` 的区别:
- **CLAUDE.md**: 全局或模块级指令，所有任务/对应目录任务都会加载
- **Skill**: 专业领域指令，仅在相关任务中按需加载

### 2. 目录结构

```
.claude/skills/<skill-name>/
├── SKILL.md          # 必须
├── example.kt        # 可选: 示例代码
└── ...
```

**命名规范**: 全小写、连字符分隔。使用前缀分类:
- `dev-` — 开发领域知识（如 `dev-config`, `dev-invui`）
- `meta-` — 关于 skill/文档创建本身的指导
- `contrib-` — 贡献/PR 规范

### 3. SKILL.md 格式

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

### 4. 何时创建 Skill vs 更新 CLAUDE.md

| 场景 | 做法 |
|---|---|
| 某领域需要专业指令 (>50 行) | 创建 Skill |
| 简短的全局规则 (编码风格、命名约定) | 添加到根目录 `CLAUDE.md` |
| 模块级编码范式 | 添加到对应子目录 `CLAUDE.md` |
| 已有 Skill 覆盖的领域需补充 | 更新现有 `SKILL.md` |

### 5. 验证清单

- [ ] 目录名全小写、带前缀、连字符分隔
- [ ] 文件名为 `SKILL.md`
- [ ] frontmatter 含 `name` 和 `description`，`name` 与目录名一致
- [ ] `description` 含 "Use this skill when..."
- [ ] 正文中文，代码块标注语言
- [ ] 文件以恰好一个换行符结尾

---

## 第二部分: 创建文档型 Skill 的流程

文档型 Skill 的目标: 当 AI 需要使用某个模块/框架时，skill 提供足够上下文，无需反复探索。

### 6. 文档型 Skill 应包含的内容

| 内容 | 说明 |
|---|---|
| 核心概念 | 关键抽象、术语和架构 |
| 项目中的用法范式 | 在本项目中实际怎么用，而非通用教程 |
| 代码模板 | 可直接复制修改的标准写法 |
| 完整示例 | 至少 1-2 个从项目真实代码中提取的示例 |
| 约束与注意事项 | 线程安全、生命周期、命名规范等陷阱 |
| API 快速参考 | 常用 API 简明清单（可选） |

### 7. 探索流程

**Step 1: 确定目标** — 明确模块名称、类型（自研/第三方/混合）、对应的 Gradle 模块和包路径。

**Step 2: 探索项目内的使用方式** — 理解本项目实际怎么用该模块:
- 浏览目录结构，了解代码组织
- 搜索 import 语句，找到所有使用者
- 完整阅读 2-3 个典型使用者（选不同复杂度的）
- 阅读封装层/工具类源码

首选 Serena（语言感知代码分析），备选 Grep/Bash/Read。

**Step 3: 探索模块本身的 API**（第三方库） — 理解原始设计意图:
- Context7 获取库文档（`mode="code"` 看 API，`mode="info"` 看概念）
- WebFetch 爬取官方文档
- Serena `search_deps=true` 搜索依赖中的符号

**Step 4: 提取范式** — 将探索结果提炼为范式:
1. 对比多个使用者，找出共同代码结构
2. 区分固定部分（初始化、生命周期）和可变部分（参数、配置）
3. 注意项目的封装层 — 文档中用封装后的 API，而非原始 API
4. 记录约束: 线程安全、可空性、生命周期限制

### 8. 推荐章节大纲

```markdown
---
name: <skill-name>
description: >
  ...
---

# <标题>

本技能指导你...

---

## 1. 核心概念
   - 术语表（表格）
   - 架构概览
   - 模块位置（Gradle 模块、包路径）

## 2. 使用范式
   ### 2.1 范式 A: <名称>
   ### 2.2 范式 B: <名称>
   （每个范式: 说明 + 代码模板 + 关键点）

## 3. 初始化 / 生命周期

## 4. API 快速参考（可选）

## 5. 完整示例: <场景 A>

## 6. 完整示例: <场景 B>

## 7. 注意事项
```

### 9. 容量控制

- 目标: **300-700 行**（当前项目 skill 参考: `dev-invui` 638 行, `dev-config` 537 行）
- 超 800 行考虑拆分
- 优先级: 范式 > 完整示例 > API 参考 > 概念介绍
- 避免大段引用原始文档 — 用搜索工具可随时获取

### 10. 技能列表

当前项目已有的 Skill:

| 目录 | 用途 |
|---|---|
| `.claude/skills/dev-config/` | 配置文件读取与编写 (Configurate + lazyconfig) |
| `.claude/skills/dev-enchantments/` | 附魔框架架构与开发指南 |
| `.claude/skills/dev-invui/` | 使用 InvUI2 创建 GUI 菜单 |
| `.claude/skills/contrib-mythicmobs/` | MythicMobs Hook 开发与 PR 规范 |
| `.claude/skills/meta-skill-guide/` | 创建 Agent Skill 的指南（本技能） |

---

## 注意事项

1. **范式来自真实代码**: 必须从项目实际代码中提取，非凭记忆编写
2. **一个 Skill 一个领域**: 如果模块过大（>800 行），拆分为多个 skill
3. **description 是触发器**: Claude Code 完全依赖它判断是否加载，写不好会导致 skill 永远不触发
4. **版本敏感**: 第三方库 API 可能因版本变化。检查 `gradle/local.versions.toml` 确认版本
5. **工具降级**: 如果 Serena/Context7 等 MCP 工具不可用，用 Grep/Bash/Read/WebFetch 替代 — 流程不变，工具不同
6. **更新已有 skill 时同步更新本指南的 §10 技能列表**
