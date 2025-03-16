package cc.mewcraft.wakame.item2.config

// FIXME #350: 似乎没有实际作用
// 代表物品配置文件中的一部分
interface ItemConfig {

    // 在一个物品配置文件中的节点的键名
    val id: String

    // 目前存在三类物品配置:
    // 1) ItemBehavior 物品的行为
    //    实现上应该仅仅是个“标记”, 用来控制物品是否拥有某个特定的行为(逻辑)
    // 2) ItemData 物品数据的生成规则
    //    用于按照规则生成特定的 ItemData (最终会写入到物品堆叠上)
    // 3) GlobalProperty 物品的全局参数
    //    即数据绑定在配置文件定义的物品类型上, 而不是绑定在游戏世界中的物品堆叠上

}