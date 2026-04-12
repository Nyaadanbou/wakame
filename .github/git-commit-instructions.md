# Git Commit 规范

本项目遵循 [Conventional Commits](https://www.conventionalcommits.org/) 格式。

## 格式

```
<type>(<scope>): <subject>
```

- `<type>` — 必填，见下方类型表
- `<scope>` — 可选，受影响的模块名 (如 `plugin`, `mixin`, `api`, `hook-mythicmobs`)
- `<subject>` — 必填，祈使句、小写开头、不加句号

## 类型

|类型|用途|
|---|---|
|`feat`|新功能|
|`fix`|Bug 修复|
|`refactor`|重构 (不改变外部行为)|
|`perf`|性能优化|
|`docs`|文档变更|
|`style`|代码格式 (不影响逻辑)|
|`test`|测试相关|
|`build`|构建系统或依赖变更|
|`ci`|CI 配置变更|
|`chore`|其他杂项|

## 示例

```
feat(plugin): add combo skill trigger system
fix(mixin): correct damage bridge null check
refactor(api): simplify item property lookup
build: bump kotlin to 2.3.10
docs: update wakame-plugin coding guide
chore(hook-mythicmobs): remove unused imports
```

## 注意事项

- Subject 使用**英文**，祈使语气 (如 `add` 而非 `added`)
- 每条 commit 只做**一件事**
- 破坏性变更在 footer 中加 `BREAKING CHANGE:` 说明

