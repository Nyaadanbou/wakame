# Agent Instructions for Koish (Wakame)

本文档为 AI Agent 提供全局引导信息。模块级编码范式见 `.github/instructions/` 下的对应文件。

## GitHub 仓库信息

|字段|值|
|---|---|
|owner|`Nyaadanbou`|
|repo|`wakame`|
|SSH|`git@github.com:Nyaadanbou/wakame.git`|

> 使用 GitHub MCP 工具时，`owner` 和 `repo` 参数分别填写以上值。

---

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

---

## MCP 工具使用偏好

|任务|首选工具|说明|
|---|---|---|
|检索项目代码 (符号、引用、结构)|Serena|语言感知的代码分析，优于纯文本搜索|
|查询第三方库/API 文档|Context7|获取最新的库文档和代码示例|
|爬取/读取网页|Firecrawl 或 `fetch`|Firecrawl 支持 JS 渲染和结构化提取，`fetch` 适合轻量级抓取|

> 如果某个 MCP 工具不可用，退而求其次使用备选工具 (`grep_search`、`read_file` 等)。

---

## 编码风格

- 注释语言: **中文**
- KDoc: 公共 API 必须有 KDoc (中文)
- 文件末尾: 恰好一个换行符 (`\n`)，不留多余空行
- 代码折叠: 使用 `//<editor-fold desc="...">` ... `//</editor-fold>` 组织长文件
- Markdown 表格: 最小格式，不加多余空格对齐
- 导入别名: 类名冲突时使用 `import ... as`
