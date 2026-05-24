---
name: contrib-mythicmobs
description: >
  Guide for writing pull request descriptions when adding or modifying
  MythicMobs Mechanic, Condition, ItemDrop, or Placeholder in the
  wakame-hook-mythicmobs module. Use this skill when creating PRs for
  the mythicmobs hook, or when asked to document MythicMobs integration
  changes, mechanic parameters, condition parameters, drops, or placeholders.
---

# MythicMobs Hook PR 书写规范

本技能规范了在 `wakame-hooks/wakame-hook-mythicmobs` 模块中添加或修改 Mechanic、Condition、ItemDrop、Placeholder 时，PR 描述应遵循的格式。

---

## 总体结构

PR 描述按变更类型分节，每种类型使用 `##` 标题。节的排列顺序：

1. 改名（如有）
2. 调整已有功能（如有）
3. 全新功能

每节标题格式：

|场景|标题格式|
|---|---|
|批量改名|`## Mechanic 改名` / `## Condition 改名` / `## ItemDrop 改名`|
|调整已有功能|`## 调整 Mechanic: \`koish_xxx\`` / `## 调整 Condition: \`koish_xxx\``|
|全新功能|`## 全新 Mechanic: \`koish_xxx\`` / `## 全新 Condition: \`koish_xxx\``|

---

## 各类型详细格式

- **Mechanic / Condition 参数表** (含改名节、新增/已有参数)、**设计说明节**: 见 `references/mechanic-condition.md`
- **Placeholder 节**、**ItemDrop 节**: 见 `references/placeholder-itemdrop.md`
- **类型词汇表**: 见 `references/type-glossary.md`
- **完整 PR 示例**: 见 `assets/full-pr-example.md`

---

## 注意事项

1. **表格用最小格式**：不要加多余空格对齐列（遵循项目 Markdown 规范）
2. **MythicMobs 配置中的名字用 snake_case**：新增的 mechanic/condition 统一使用 `koish_` 前缀
3. **使用示例必须包含 YAML 代码块**：展示实际的 MythicMobs 技能配置
4. **改名兼容性说明**：如果旧名字仍然可用，需要在改名节中明确说明
5. **注册点**：新增的 mechanic/condition/drop 需要在 `ConfigListener.kt` 的对应 `when` 分支中注册

---

## 子文件索引

- `references/mechanic-condition.md` — Mechanic/Condition 参数表格式（模板、改名节、调整已有功能模板、设计说明节）
- `references/placeholder-itemdrop.md` — Placeholder 和 ItemDrop 节的格式
- `references/type-glossary.md` — 参数表"类型"列的术语词汇表
- `assets/full-pr-example.md` — 完整 PR 描述示例
