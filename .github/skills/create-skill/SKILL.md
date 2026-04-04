---
name: create-skill
description: >
  Guide for creating new Agent Skills in the Koish (wakame) project.
  Use this skill when asked to create, add, or scaffold a new agent skill,
  or when the task involves writing SKILL.md files, organizing skill directories,
  or setting up specialized Copilot instructions for this project.
---

# 创建新的 Agent Skill

本技能指导你在 Koish (wakame) 项目中创建新的 Agent Skill。

Agent Skill 是一组指令、脚本和资源，Copilot 会在相关任务中自动加载它们，以提升在专业任务上的表现。

---

## 1. 什么是 Agent Skill

Agent Skill 是一个包含 `SKILL.md` 文件的目录。当 Copilot 判断用户的任务与某个 Skill 的 `description` 匹配时，会自动将该 Skill 的 `SKILL.md` 内容注入上下文，使 Copilot 能够遵循其中的指令。

Skill 与 Custom Instructions (如 `copilot-instructions.md`) 的区别:
- **Custom Instructions**: 简短的全局规则，几乎所有任务都会加载 (如编码风格、项目结构概览)。
- **Skill**: 详细的专业指令，仅在相关任务中按需加载 (如 "创建 GUI 菜单"、"编写配置文件")。

---

## 2. 本项目的 Skill 存放位置

本项目使用 **project skills**，所有 Skill 存放在:

```
.github/skills/<skill-name>/SKILL.md
```

当前已有的 Skill:

| 目录 | 用途 |
|---|---|
| `.github/skills/invui-gui/` | 使用 InvUI2 创建 GUI 菜单 |
| `.github/skills/configurate-config/` | 配置文件读取与编写 (Configurate + lazyconfig) |
| `.github/skills/create-skill/` | 创建新的 Agent Skill (本技能) |
| `.github/skills/create-module-skill/` | 创建模块/框架文档型 Skill (元技能) |

---

## 3. 创建 Skill 的步骤

### 3.1 创建目录

在 `.github/skills/` 下创建一个新的子目录。目录名规则:
- **全小写**
- **用连字符 (`-`) 分隔单词**
- 名称应简短且能概括 Skill 的用途

```
.github/skills/<skill-name>/
├── SKILL.md          # 必须: Skill 指令文件
├── example.kt        # 可选: 示例代码
└── helper-script.sh  # 可选: 辅助脚本
```

### 3.2 编写 SKILL.md

`SKILL.md` 是一个带 YAML frontmatter 的 Markdown 文件。

#### YAML Frontmatter (必须)

```yaml
---
name: <skill-name>
description: >
  <Skill 的描述。说明 Skill 做什么，以及 Copilot 应在什么场景下使用它。>
---
```

| 字段 | 必须 | 说明 |
|---|---|---|
| `name` | ✅ | Skill 的唯一标识符。全小写，用连字符分隔，通常与目录名一致。 |
| `description` | ✅ | 描述 Skill 的用途和触发场景。Copilot 根据此描述判断是否加载该 Skill。 |
| `license` | ❌ | 可选的许可证信息。 |
| `allowed-tools` | ❌ | 可选。预授权 Skill 可使用的工具 (如 `shell`)，跳过确认提示。**谨慎使用。** |

#### description 编写要点

`description` 是 Copilot 判断是否加载 Skill 的唯一依据，必须写好:

1. **第一句话**: 概括 Skill 的功能 (如 "Guide for creating GUI menus using InvUI2")
2. **第二句话**: 用 "Use this skill when..." 明确触发场景
3. **包含关键词**: 列出相关的技术术语，确保 Copilot 能匹配到

好的示例:
```yaml
description: >
  Guide for creating GUI menus using InvUI2 in the Koish (wakame) project.
  Use this skill when asked to create, modify, or debug GUI/menu code,
  or when the task involves InvUI windows, guis, items, inventories,
  or any Minecraft inventory-based UI in the wakame-plugin module.
```

不好的示例:
```yaml
description: GUI stuff  # 太模糊，缺少触发场景
```

#### Markdown 正文

正文是 Skill 的核心指令。遵循以下结构:

1. **标题和概述**: 简明说明本技能教什么
2. **核心概念**: 解释关键术语和架构
3. **范式/模板**: 提供标准代码模板和写法
4. **完整示例**: 给出可直接参考的完整代码
5. **注意事项**: 列出常见陷阱和最佳实践

### 3.3 正文编写规范

以下规范适用于本项目的所有 Skill:

- **语言**: 正文用**中文**编写 (与项目注释语言一致)
- **frontmatter**: `name` 和 `description` 用**英文**编写 (供 Copilot 匹配用)
- **代码块**: 使用 ` ```kotlin ` 标注语言
- **文件末尾**: 以恰好一个换行符 (`\n`) 结尾
- **结构层次**: 使用编号章节 (`## 1.`, `## 2.`, ...) 组织内容
- **表格**: 用 Markdown 表格总结对照信息
- **示例代码**: 优先使用本项目的真实代码模式，而非通用示例
- **禁用字符画**: 不要使用 ASCII art / box-drawing 字符画（如 `┌──┐`、`├──┤` 等）。Skill 是给 LLM 读的，字符画只会浪费 token。用编号列表或表格替代。

---

## 4. 何时创建新 Skill vs. 更新现有文件

| 场景 | 做法 |
|---|---|
| 某类任务需要详细的专业指令 (>50 行) | 创建新的 Skill |
| 简短的全局规则 (编码风格、命名约定) | 添加到 `copilot-instructions.md` |
| 项目架构级别的范式总结 | 添加到 `AGENTS.md` |
| 已有 Skill 覆盖的领域需要补充 | 更新现有 Skill 的 `SKILL.md` |

**经验法则**: 如果指令足够专业、足够长，只有在特定任务才需要用到，就应该做成 Skill。

---

## 5. 完整示例: 创建一个新 Skill

假设要创建一个 "编写 ItemProp" 的 Skill:

### 5.1 创建目录和文件

```
.github/skills/item-prop/
└── SKILL.md
```

### 5.2 编写 SKILL.md

````markdown
---
name: item-prop
description: >
  Guide for creating new ItemProp data types in the Koish (wakame) project.
  Use this skill when asked to create, register, or modify ItemProp types,
  or when the task involves item properties, ItemPropTypes, or property
  data classes in wakame-mixin.
---

# 创建新的 ItemProp

本技能指导你在 Koish (wakame) 项目中创建和注册新的 ItemProp 数据类型。

---

## 1. ItemProp 概述

`ItemProp` 是附加在物品类型上的静态数据，定义在 `wakame-mixin` 中。
每个 ItemProp 由一个 `@ConfigSerializable` data class 和一个注册在
`ItemPropTypes` 中的类型声明组成。

## 2. 创建步骤

### 2.1 定义数据类型

在 `wakame-mixin/src/main/kotlin/cc/mewcraft/wakame/item/property/impl/`
下创建 data class:

```kotlin
@ConfigSerializable
data class MyPropData(
    val someValue: Int = 0,
    val anotherValue: String = "default",
)
```

### 2.2 注册到 ItemPropTypes

在 `ItemPropTypes.kt` 中添加:

```kotlin
@JvmField
val MY_PROP: ItemPropType<MyPropData> = typeOf("my_prop") {
    serializers {
        register(MyCustomSerializer()) // 如需自定义序列化器
    }
}
```

### 2.3 读取 ItemProp

```kotlin
val propValue = itemStack.getProp(ItemPropTypes.MY_PROP) ?: return
```

## 3. 注意事项

1. data class 属性用 camelCase，YAML 自动转 snake_case
2. 提供合理的默认值
3. 子目录按类别组织 (如 `weapon/`)
````

### 5.3 验证清单

创建完成后，确认以下几点:

- [ ] 目录名全小写、用连字符分隔
- [ ] 文件名为 `SKILL.md` (大写，不可更改)
- [ ] frontmatter 包含 `name` 和 `description`
- [ ] `name` 与目录名一致
- [ ] `description` 包含 "Use this skill when..." 触发场景
- [ ] 正文用中文编写
- [ ] 代码块标注语言
- [ ] 文件以恰好一个换行符结尾

---

## 6. 包含脚本的 Skill

如果 Skill 需要运行脚本:

1. 将脚本放在 Skill 目录下
2. 在 `SKILL.md` 正文中说明何时和如何运行脚本
3. 如需跳过确认提示，在 frontmatter 中添加 `allowed-tools`:

```yaml
---
name: my-skill
description: ...
allowed-tools: shell
---
```

> **警告**: 只有在完全信任脚本内容时才使用 `allowed-tools: shell`。
> 预授权 shell 会跳过执行终端命令的确认步骤。

---

## 7. 注意事项

1. **每个 Skill 一个目录**: 不要在同一目录下放多个 Skill。
2. **SKILL.md 不可改名**: 文件必须命名为 `SKILL.md`，Copilot 只识别这个文件名。
3. **description 是触发器**: Copilot 完全依赖 `description` 判断是否加载 Skill，写不好会导致 Skill 永远不被触发。
4. **避免过大的 Skill**: Skill 加载后会占用上下文窗口。如果内容过多，考虑拆分为多个 Skill。
5. **Skill 目录下的所有文件都可被 Copilot 访问**: 当 Skill 被触发时，Copilot 可以读取目录下的所有文件，因此可以放置示例代码、模板等辅助资源。

