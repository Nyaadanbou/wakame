package cc.mewcraft.wakame.item2.data.impl

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class ItemLevel(
    // FIXME #350: 验证这样是否可以生成一个 IntTag 而非 CompoundTag
    @Setting(nodeFromParent = true)
    val level: Int,
) {

    companion object {
        // FIXME #350: 读取全局配置文件
        val minimumLevel: Int = 1
    }

}
