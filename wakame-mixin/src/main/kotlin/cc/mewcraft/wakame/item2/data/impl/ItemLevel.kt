package cc.mewcraft.wakame.item2.data.impl

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class ItemLevel(
    val level: Int,
) {

    companion object {
        // FIXME #350: 读取全局配置文件
        val minimumLevel: Int = 1
    }

}
