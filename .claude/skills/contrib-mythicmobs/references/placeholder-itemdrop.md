# Placeholder / ItemDrop PR 书写规范

本文件描述 MythicMobs Placeholder 和 ItemDrop 的 PR 节格式。Mechanic/Condition 格式见 `references/mechanic-condition.md`。

---

## Placeholder 节

Placeholder 不使用参数表格，而是描述其功能和用法：

````markdown
## 全新占位符: `<koish.xxx>`

返回某个数值的描述。

### 参数

|名字|类型|说明|
|---|---|---|
|`arg1`|字符串|参数说明|

### 示例

```yaml
- message{m="Value: <koish.xxx.arg1>"} @self
```
````

---

## ItemDrop 节

````markdown
## 全新 ItemDrop: `koish_xxx`

简要描述该 drop 的用途。

### 参数

|名字|别名|类型|默认|说明|
|---|---|---|---|---|
|`param`|`p`|字符串|无|参数说明|
````
