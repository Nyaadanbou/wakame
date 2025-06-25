## Items

你可以在文件夹 `items/` 下通过创建一个新的文件的形式向游戏内添加一个新的 Koish 物品类型.

## 文件名规范

创建的文件类型均为 `YAML`. 文件名必须为 `<id>.yml` 的形式, 其中 `id` 必须为以下字符的组合:

- `0123456789` Numbers
- `abcdefghijklmnopqrstuvwxyz` Lowercase letters
- `_` Underscore
- `-` Hyphen/minus
- `.` Dot
- `/` Forward Slash

## 文件夹规范

你可以通过创建文件夹的方式对物品文件 (.yml) 进行分类.

文件夹的名字必须是以下字符的组合 (注意没有 `/`):

- `0123456789` Numbers
- `abcdefghijklmnopqrstuvwxyz` Lowercase letters
- `_` Underscore
- `-` Hyphen/minus
- `.` Dot

## 物品 ID

当你创建好若干文件/文件夹后, 物品会根据其文件名和其所在的文件夹生成属于它自己的 ID.
了解一个物品 ID 的生成规则是非常重要的, 因为你大概率会在 Koish 的其他系统中用到.
例如, 当你需要在游戏内查看创建的新物品时, 你就需要使用指令来指定一个物品 ID, 才能在游戏内把物品创建出来.

例如在 `items/` 下存在这样的一个文件结构:

- `items/`
    - `example/`
        - `yummy_apple.yml`
        - `yummy_pear.yml`
    - `internal/`
        - `unknown.yml`

一共有2个文件夹: `example` 和 `internal`.

以及3个文件 (.yml): `yummy_apple.yml`, `yummy_pear.yml`, `unknown.yml`.

这3个文件对应3个物品, 它们的 ID 分别为:

- `koish:example/yummy_apple`
- `koish:example/yummy_pear`
- `koish:internal/unknown`

## 物品格式

看到这里, 你应该已经了解如何创建一个新物品, 文件和文件夹的命名规范, 以及它们分别意味着什么.
但尚未了解一个物品文件里面的具体内容如何编写. 例如, 怎么设置物品的外观? 怎么使物品拥有攻击力?
这些问题都属于“物品格式”的范畴. 接下来我们将详细讲解物品格式.
