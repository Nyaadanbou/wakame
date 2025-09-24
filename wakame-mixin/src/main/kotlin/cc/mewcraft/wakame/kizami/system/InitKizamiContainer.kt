package cc.mewcraft.wakame.kizami.system

import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.kizami.KizamiMap
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.IteratingSystem

/**
 * 初始化玩家的铭刻容器.
 */
object InitKizamiContainer : IteratingSystem(
    family = EWorld.family { all(BukkitObject, BukkitPlayer) }
), FamilyOnAdd {
    override fun onTickEntity(entity: Entity) = Unit

    override fun onAddEntity(entity: Entity) {
        // 创建 kizami container
        val kizamiContainer = KizamiMap.create()

        // 添加到 ecs entity
        entity.configure { it += kizamiContainer }
    }
}