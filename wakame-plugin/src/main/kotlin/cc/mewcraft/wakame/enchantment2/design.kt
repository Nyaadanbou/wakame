package cc.mewcraft.wakame.enchantment2

// FIXME #365: 整体设计:
//  数据分为几大类:
//    1) 储存在 Datapacks 中的配置文件, 这些数据从服务器启动直到关闭都是不变的, 必须在 NMS 中注册相应的 Codec
//       也许采取类似物品储存数据的方式比较好?
//    2) 储存在 ECS entity 上的数据, 这些数据会在“附魔效果”开始生效时创建, 并且可能会在运行时被更新和移除
//
