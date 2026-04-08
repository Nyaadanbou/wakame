package cc.mewcraft.wakame.item.data.impl

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class ItemLevel(
    @Setting(nodeFromParent = true)
    val level: Int,
) {

    companion object {
        const val MINIMUM_LEVEL: Int = 1 // FIXME #350: 读取全局配置文件
    }

}
