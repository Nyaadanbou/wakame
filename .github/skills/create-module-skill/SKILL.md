---
name: create-module-skill
description: >
  Meta-skill for creating Agent Skills that document in-house modules, frameworks,
  or libraries in the Koish (wakame) project. Use this skill when asked to create
  a new skill that introduces, explains, or documents the usage, constraints,
  and patterns of a specific module or framework within this project (e.g., a skill
  like invui-gui that explains how to use a particular library/module). This skill
  guides the AI through project exploration, dependency analysis, and skill authoring.
---

# 创建模块/框架文档型 Agent Skill

本技能指导你创建**模块/框架文档型 Skill** — 即那些用来介绍本项目中某个特定自研模块或第三方框架的用法、约束和范式的 Skill（典型例子: `invui-gui` Skill）。

> **与 `create-skill` 的区别**: `create-skill` 教你从零创建任意类型的 Skill（目录结构、frontmatter 格式等通用流程）。本技能专注于**内容产出流程** — 即如何系统地探索一个模块/框架，提取关键信息，最终组织成一个高质量的文档型 Skill。

---

## 1. 文档型 Skill 的特征

文档型 Skill 的目标是：当 AI 需要在本项目中使用某个模块/框架时，Skill 能提供足够的上下文，使 AI 无需反复探索即可正确编码。

一个好的文档型 Skill 应包含:

| 内容 | 说明 |
|---|---|
| **核心概念** | 模块的关键抽象、术语和架构 |
| **本项目的用法范式** | 在本项目中实际怎么用，而非通用教程 |
| **代码模板** | 可直接复制和修改的标准写法 |
| **完整示例** | 至少 1-2 个从本项目真实代码中提取的完整示例 |
| **约束与注意事项** | 线程安全、生命周期、命名规范等陷阱 |
| **API 快速参考** | 常用 API 的简明清单 |

---

## 2. 创建流程总览

1. **确定目标模块/框架**
2. **探索项目内的使用方式** — Serena (代码结构、符号、引用), grep/find (使用模式、导入)
3. **探索模块本身的 API 和文档** — Context7 (库文档), Firecrawl/fetch (网页文档), Serena search_deps (依赖符号)
4. **提取范式** — 从真实代码中总结标准写法
5. **组织并编写 SKILL.md**
6. **验证与完善**

---

## 3. Step 1: 确定目标模块/框架

明确要文档化的目标:

- **模块名称**: 在项目中的称呼（如 "InvUI2"、"Configurate"、"MetadataMap"）
- **模块类型**:
  - **自研模块**: 代码完全在本项目内（如 `wakame-mixin` 中的某个子系统）
  - **第三方库**: 通过依赖引入（如 InvUI2、Configurate）
  - **混合型**: 第三方库 + 本项目的封装层
- **对应的 Gradle 模块**: 明确代码在哪个模块中（`wakame-mixin`、`wakame-plugin`、`common/lazyconfig` 等）

---

## 4. Step 2: 探索项目内的使用方式

这一步的目标是理解**本项目实际上是怎么使用该模块的**。

### 4.1 使用 Serena 探索代码结构（首选）

Serena 提供了语言感知的代码分析能力，是探索代码结构的首选工具。

```
# 查看目标目录下的符号概览
mcp_oraios_serena_get_symbols_overview(relative_path="wakame-plugin/src/main/kotlin/cc/mewcraft/wakame/gui/")

# 查找特定符号的定义
mcp_oraios_serena_find_symbol(name_path_pattern="BasicMenuSettings", include_body=true)

# 查找某个符号的所有引用 — 理解使用模式
mcp_oraios_serena_find_referencing_symbols(name_path="MyClass", relative_path="path/to/file.kt")

# 搜索代码模式
mcp_oraios_serena_search_for_pattern(substring_pattern="PagedGui\\.itemsBuilder", restrict_search_to_code_files=true)

# 浏览目录结构
mcp_oraios_serena_list_dir(relative_path="wakame-mixin/src/main/kotlin/cc/mewcraft/wakame/", recursive=false)
```

**关键策略**:
- 先用 `list_dir` 掌握目录结构
- 用 `get_symbols_overview` 快速了解文件中的类/函数
- 用 `find_symbol(depth=1)` 查看类的成员列表
- 用 `find_referencing_symbols` 追踪使用模式 — 这是理解范式的关键
- 用 `search_for_pattern` 搜索特定的代码模式

### 4.2 使用 grep/find 补充搜索

当需要搜索字符串模式或非代码文件时:

```
# 搜索 import 语句，了解哪些文件使用了目标模块
grep_search(query="import xyz.xenondevs.invui", includePattern="*.kt")

# 搜索特定 API 的使用方式
grep_search(query="PagedGui.itemsBuilder", includePattern="*.kt")

# 查找配置文件
file_search(query="**/gui/*.yml")
```

### 4.3 读取关键源文件

确定了关键文件后，使用 `read_file` 完整阅读:

- 读取 2-3 个**典型使用者**的完整源码（选择不同复杂度的）
- 读取封装层/工具类的源码（如 `BasicMenuSettings`、`SlotDisplay` 等）
- 读取相关的配置文件样例

> **注意**: 优先读取大段有意义的内容，而非进行很多次小范围读取。

---

## 5. Step 3: 探索模块本身的 API 和文档

这一步的目标是理解模块的**原始 API 和设计意图**。

### 5.1 使用 Context7 获取库文档（首选，适用于第三方库）

Context7 提供最新的库文档，是了解第三方库 API 的首选方式。

```
# 1. 先解析库 ID
mcp_io_github_ups_resolve-library-id(libraryName="InvUI")

# 2. 获取文档
mcp_io_github_ups_get-library-docs(
    context7CompatibleLibraryID="/xenondevs/invui",
    topic="PagedGui",
    mode="code"  # API 参考和代码示例用 "code"
)

# 3. 如需概念性介绍
mcp_io_github_ups_get-library-docs(
    context7CompatibleLibraryID="/xenondevs/invui",
    topic="getting started",
    mode="info"  # 概念指南和架构说明用 "info"
)
```

**使用策略**:
- `mode="code"`: 获取 API 参考、方法签名、代码示例
- `mode="info"`: 获取概念指南、架构说明、设计理念
- 如果一个 topic 内容不够，尝试 `page=2`, `page=3` 获取更多
- 可以多次调用不同 topic 来覆盖模块的各个方面

### 5.2 使用 Firecrawl 爬取网页文档（首选的网页爬取工具）

当需要从官方文档网站获取信息时，优先使用 Firecrawl:

```
# 搜索官方文档
mcp_firecrawl_fir_firecrawl_search(query="InvUI2 GUI library documentation")

# 爬取特定文档页面
mcp_firecrawl_fir_firecrawl_scrape(url="https://docs.example.com/api", formats=["markdown"], onlyMainContent=true)

# 发现文档站点的所有页面
mcp_firecrawl_fir_firecrawl_map(url="https://docs.example.com", search="PagedGui")
```

### 5.3 使用 fetch 作为轻量级替代

当只需要快速抓取单个页面时:

```
mcp_fetch_fetch(url="https://wiki.example.com/api-reference", max_length=10000)
```

### 5.4 使用 Serena 搜索依赖中的符号

对于通过 Gradle 引入的依赖，可以用 Serena 的 JetBrains 集成直接搜索依赖中的符号:

```
# 在依赖中搜索符号定义
mcp_oraios_serena_jet_brains_find_symbol(
    name_path_pattern="PagedGui",
    search_deps=true,
    include_info=true  # 获取签名和文档注释
)

# 查看依赖类的成员列表
mcp_oraios_serena_jet_brains_find_symbol(
    name_path_pattern="VirtualInventory",
    search_deps=true,
    depth=1  # 获取成员
)

# 查看类型层次结构
mcp_oraios_serena_jet_brains_type_hierarchy(
    name_path="ItemBehavior",
    relative_path="wakame-mixin/src/main/kotlin/.../ItemBehavior.kt",
    hierarchy_type="sub"
)
```

### 5.5 工具优先级总结

| 场景 | 首选工具 | 备选工具 |
|---|---|---|
| 探索项目代码结构 | Serena (`get_symbols_overview`, `find_symbol`, `search_for_pattern`) | `grep_search`, `file_search`, `read_file` |
| 查找符号引用/使用 | Serena (`find_referencing_symbols`) | `grep_search` |
| 获取第三方库文档 | Context7 (`resolve-library-id` + `get-library-docs`) | Firecrawl search → scrape |
| 爬取文档网页 | Firecrawl (`firecrawl_scrape`, `firecrawl_search`) | `fetch` |
| 搜索依赖中的符号 | Serena JetBrains (`jet_brains_find_symbol` + `search_deps`) | Context7 |
| 浏览目录结构 | Serena (`list_dir`) | `list_dir`, `file_search` |
| 读取源代码 | `read_file` | Serena (`find_symbol` + `include_body`) |

> **如果某个 MCP 工具不可用**，退而求其次使用备选工具。核心目标不变：充分理解模块的 API 和本项目中的使用方式。

---

## 6. Step 4: 提取范式

将探索中收集到的信息提炼为**范式（Pattern）**。

### 6.1 识别范式的方法

1. **对比多个使用者**: 阅读 3+ 个使用了目标模块的文件，找出**共同的代码结构**
2. **区分固定部分和可变部分**:
   - 固定: 初始化方式、必须调用的 API、生命周期管理
   - 可变: 业务参数、配置内容、数据来源
3. **注意本项目的封装**: 很多模块有项目级的封装（如 `BasicMenuSettings` 封装了 InvUI 的配置）。文档中应使用封装后的 API，而非原始 API
4. **收集约束**: 线程安全要求、可空性、生命周期限制、命名规范等

### 6.2 范式提取清单

对每个识别出的范式，记录:

- [ ] **范式名称**: 简短描述（如 "翻页按钮标准写法"）
- [ ] **适用场景**: 什么时候用
- [ ] **代码模板**: 可复制的骨架代码
- [ ] **可变参数**: 使用者需要替换的部分
- [ ] **注意事项**: 常见错误或陷阱

---

## 7. Step 5: 组织并编写 SKILL.md

### 7.1 标准目录结构

```
.github/skills/<module-name>/
├── SKILL.md          # 必须: Skill 指令文件
└── example.kt        # 可选: 较长的完整示例
```

### 7.2 SKILL.md 推荐大纲

以下是文档型 Skill 的推荐章节结构（参考 `invui-gui` Skill）:

```markdown
---
name: <module-name>
description: >
  Guide for using <Module> in the Koish (wakame) project.
  Use this skill when asked to <触发场景列表>,
  or when the task involves <关键术语列表>.
---

# <模块标题>

本技能指导你在 Koish (wakame) 项目中使用 <模块> …

---

## 1. 核心概念
   - 术语表（用表格）
   - 架构概览
   - 模块在项目中的位置（哪个 Gradle 模块、哪个包路径）

## 2. 本项目的使用范式
   ### 2.1 范式 A: <名称>
   ### 2.2 范式 B: <名称>
   ### 2.N ...
   (每个范式包含: 说明 + 代码模板 + 关键点)

## 3. 初始化 / 生命周期
   - 模块如何初始化
   - 是否需要手动初始化
   - 清理/销毁逻辑

## 4. API 快速参考
   - 常用类和方法的简明清单
   - 方法签名 + 一行说明

## 5. 完整示例: <场景 A>
   (从项目真实代码中提取，简化后展示)

## 6. 完整示例: <场景 B>
   (不同复杂度或不同用途的示例)

## 7. 注意事项
   1. 线程安全
   2. 生命周期管理
   3. 命名规范
   4. 常见错误
   ...
```

### 7.3 编写要点

- **frontmatter 用英文**，正文用**中文**
- **description 必须包含**: 功能概括 + "Use this skill when..." + 关键术语
- **代码示例优先使用本项目的真实代码模式**，而非库的通用示例
- **每个代码块标注语言**: ` ```kotlin `
- **使用表格**总结对照信息（类型列表、API 清单等）
- **代码模板**中用注释标出可变部分: `// ← 替换为实际业务逻辑`
- **文件以恰好一个换行符 (`\n`) 结尾**
- **禁用字符画**: 不要使用 ASCII art / box-drawing 字符画（如 `┌──┐`、`├──┤` 等）。Skill 是给 LLM 读的，字符画只会浪费 token。用编号列表或表格替代。

### 7.4 容量控制

Skill 加载后会占用上下文窗口，需要控制大小:

- **目标**: 300-700 行（参考: `invui-gui` 为 638 行）
- **超过 800 行**: 考虑拆分为多个 Skill
- **优先级**: 范式 > 完整示例 > API 参考 > 概念介绍
- **避免**: 大段引用库的原始文档（用 Context7 可以按需获取）

---

## 8. Step 6: 验证与完善

### 8.1 验证清单

创建完成后，逐项确认:

**结构检查:**
- [ ] 目录名全小写、用连字符分隔
- [ ] 文件名为 `SKILL.md`（大写）
- [ ] frontmatter 包含 `name` 和 `description`
- [ ] `name` 与目录名一致
- [ ] `description` 包含 "Use this skill when..." 和关键术语

**内容检查:**
- [ ] 正文用中文编写
- [ ] 代码块标注语言（` ```kotlin `）
- [ ] 包含核心概念章节
- [ ] 包含至少 2 个使用范式
- [ ] 包含至少 1 个完整示例（从真实代码提取）
- [ ] 包含注意事项章节
- [ ] 代码示例使用了本项目的封装（而非原始 API）

**格式检查:**
- [ ] 文件以恰好一个换行符结尾
- [ ] 没有多余空行
- [ ] 表格对齐、代码块缩进正确

### 8.2 更新已有文件

创建新 Skill 后，更新 `create-skill/SKILL.md` 中的 "当前已有的 Skill" 表格（§2）:

```markdown
| `.github/skills/<new-skill>/` | <用途描述> |
```

---

## 9. 完整工作流示例

以创建 `invui-gui` Skill 为例，展示完整工作流:

### 9.1 确定目标

- 模块: InvUI2（第三方 GUI 库）
- 类型: 混合型（第三方库 + 本项目封装）
- Gradle 模块: `wakame-plugin`
- 包路径: `cc.mewcraft.wakame.gui`

### 9.2 探索项目使用方式

```
# 1. 浏览 GUI 目录结构
serena.list_dir("wakame-plugin/src/main/kotlin/cc/mewcraft/wakame/gui/", recursive=true)

# 2. 查看菜单类的结构
serena.get_symbols_overview("wakame-plugin/.../gui/CatalogItemBrowserMenu.kt", depth=1)

# 3. 搜索所有使用 PagedGui 的文件
serena.search_for_pattern("PagedGui\\.itemsBuilder", restrict_search_to_code_files=true)

# 4. 查找 BasicMenuSettings 的所有引用
serena.find_referencing_symbols("BasicMenuSettings", "wakame-plugin/.../gui/BasicMenuSettings.kt")

# 5. 完整阅读 2-3 个典型菜单类
read_file("wakame-plugin/.../gui/CatalogItemBrowserMenu.kt")
read_file("wakame-plugin/.../gui/ReforgeMenu.kt")
```

### 9.3 探索 InvUI2 本身

```
# 1. Context7 获取 InvUI 文档
resolve_library_id("InvUI")
get_library_docs("/xenondevs/invui", topic="Gui", mode="code")
get_library_docs("/xenondevs/invui", topic="Window", mode="code")

# 2. 搜索依赖中的符号
serena.jet_brains_find_symbol("PagedGui", search_deps=true, depth=1)
serena.jet_brains_find_symbol("VirtualInventory", search_deps=true, include_info=true)
```

### 9.4 提取范式

从阅读的代码中识别出:
- 菜单类基本结构（class + viewer + settings + gui + window + open）
- Structure 字符串布局
- BasicMenuSettings 配置读取
- 翻页按钮标准写法
- 滚动按钮标准写法
- VirtualInventory 输入/输出
- Window 生命周期钩子
- PlayerInventorySuppressor
- 菜单栈导航

### 9.5 编写 SKILL.md

按照 §7.2 的推荐大纲组织内容，最终产出 `invui-gui/SKILL.md`。

---

## 10. 注意事项

1. **不要跳过探索步骤**: 即使你对库有一定了解，也要通过探索确认本项目的实际用法。项目可能有自己的封装和约定。
2. **范式来自真实代码**: 所有范式必须从项目中的真实代码中提取，而非凭记忆编写。
3. **保持 Skill 的聚焦**: 一个 Skill 只覆盖一个模块/框架。如果模块过大，可以拆分（如 `module-basics` 和 `module-advanced`）。
4. **工具不可用时的降级策略**: 如果 Serena、Context7 或 Firecrawl 不可用，使用 `grep_search`、`read_file`、`file_search`、`fetch` 等基础工具完成同样的任务——核心流程不变，只是工具效率不同。
5. **版本敏感**: 第三方库的 API 可能因版本不同而差异较大。通过检查 `gradle/local.versions.toml` 确认库的版本，并在 Context7 / 文档中查看对应版本的 API。
6. **中文注释**: 与本项目一致，Skill 正文和代码注释均使用中文。




