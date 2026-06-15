# Agent Instructions for Koish

本文档为 Claude Code 提供全局引导信息。模块级编码范式见各子目录下的 `CLAUDE.md`。

## 模块职责概览

|模块|职责|
|---|---|
|`buildSrc`|Gradle 构建约定|
|`common/lazyconfig`|配置框架 (`MAIN_CONFIG`, `entryOrElse`, `optionalEntry`)|
|`common/messaging`|跨服消息工具|
|`standalone/*`|独立子插件 (经济、定时任务等)|
|`wakame-api`|对外暴露的公共 API|
|`wakame-mixin`|NMS Mixin 补丁 + Bridge 接口。使用 Horizon + Weaver 构建。**不含游戏逻辑。**|
|`wakame-plugin`|**所有游戏逻辑**: 物品系统、配置读取、事件监听、tick 系统、Feature 实现|
|`wakame-hooks/*`|第三方插件集成 (每个 hook 一个子模块)|

## 构建系统

- **Gradle** with Kotlin DSL (`build.gradle.kts`)
- Version catalog: `gradle/local.versions.toml`
- Root settings: `settings.gradle.kts`
- **Paper 1.21+** 服务器

## 构建命令

| 命令 | 说明 |
|---|---|
| `./gradlew :wakame-plugin:shadowJar` | 构建插件主产物 (shadow jar，最终可加载的插件) |
| `./gradlew :wakame-plugin:build` | 构建并测试 wakame-plugin |
| `./gradlew build` | 构建全部模块 |

## 关键技术约定

- **语言**: Kotlin (JVM 21)
- **配置格式**: YAML, 通过 Configurate + `@ConfigSerializable` 反序列化。`NamingSchemes.SNAKE_CASE` 自动映射 (camelCase Kotlin 属性 ↔ snake_case YAML 键)
- **主配置文件**: `wakame-plugin/src/main/resources/configs/config.yml`
- **Git commits**: Conventional Commits 格式 (详见 `.github/git-commit-instructions.md`)

## MCP 工具使用偏好

|任务|首选工具|说明|
|---|---|---|
|检索项目代码 (符号、引用、结构)|codegraph|语言感知的代码图谱，优于纯文本搜索；先查后写|
|查询第三方库/API 文档|Context7|获取最新的库文档和代码示例|
|爬取/读取网页|fetch 或 WebFetch|轻量级网页抓取与结构化提取|

> 如果某个 MCP 工具不可用，退而求其次使用 Grep / Bash / Read 等内置工具。

## 编码风格

- 注释语言: **中文**
- KDoc: 公共 API 必须有 KDoc (中文)
- 文件末尾: 恰好一个换行符 (`\n`)，不留多余空行
- 代码折叠: 使用 `//<editor-fold desc="...">` ... `//</editor-fold>` 组织长文件
- Markdown 表格: 最小格式，不加多余空格对齐
- 导入别名: 类名冲突时使用 `import ... as`
- Feature 类: 标记 `@Init(InitStage.POST_WORLD)` + `@InitFun`，使用 `object` + `Listener`
- 配置读取: 优先 `entryOrElse(default, vararg path)` 带默认值

## 行为准则

以下准则偏向谨慎而非速度。对琐碎任务可自行判断。

### 1. 先想后写

**不要假设。不要隐藏困惑。主动暴露权衡。**

实现之前:
- 明确陈述你的假设。如果不确定，直接问。
- 如果存在多种解读，列出来——不要默默选一个。
- 如果有更简单的方法，说出来。必要时坚持己见。
- 如果卡住了，停下来。说明困惑所在。提问。

### 2. 简洁优先

**最少的代码解决问题。不做投机性开发。**

- 不添加未被要求的功能。
- 不为一次性用例创建抽象。
- 不做未被要求的"灵活性"或"可配置性"。
- 不为不可能发生的场景做错误处理。
- 如果写了 200 行、50 行就能搞定，重写。

自问: "一个资深工程师会觉得这过度复杂吗？" 如果是，简化。

### 3. 精准修改

**只改必须改的。只清理自己造成的烂摊子。**

编辑已有代码时:
- 不要"顺手改进"相邻代码、注释或格式。
- 不要重构没有坏的东西。
- 匹配已有风格，即使你更喜欢另一种写法。
- 如果注意到无关的死代码，提出来——但不要删。

当你的改动产生孤立代码时:
- 移除由你的改动造成的未使用的 import/变量/函数。
- 不要删除已有的死代码，除非被要求。

检验标准: 每一行改动都应该能直接追溯到用户的请求。

### 4. 目标驱动执行

**定义成功标准。循环直到验证通过。**

将任务转化为可验证的目标:
- "加验证" → "为非法输入写测试，然后让测试通过"
- "修 bug" → "写一个能复现它的测试，然后修好"
- "重构 X" → "确保测试前后都通过"

多步骤任务，列出简要计划:
```
1. [步骤] → 验证: [检查项]
2. [步骤] → 验证: [检查项]
3. [步骤] → 验证: [检查项]
```

强成功标准能让你独立循环推进。弱标准 ("让它能跑") 需要不断回来澄清。
