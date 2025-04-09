package cc.mewcraft.wakame.hook.impl.mythicmobs

import cc.mewcraft.wakame.damage.DamageApplier
import cc.mewcraft.wakame.hook.impl.mythicmobs.listener.ConfigListener
import cc.mewcraft.wakame.hook.impl.mythicmobs.listener.DamageListener
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.mixin.support.MythicApi
import cc.mewcraft.wakame.mixin.support.MythicApiProvider
import cc.mewcraft.wakame.util.registerEvents
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.core.constants.MobKeys
import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.persistence.PersistentDataType

@Hook(plugins = ["MythicMobs"])
object MythicMobsHook {
    init {
        // 注册 Listeners
        ConfigListener.registerEvents()
        DamageListener.registerEvents()

        // 注册 DamageApplier
        // 这应该覆盖掉默认的实例
        DamageApplier.register(MythicMobsDamageApplier)
        MythicApiProvider.register(MythicApiImpl)
    }
}

private object MythicApiImpl : MythicApi {
    private val MYTHIC_API: MythicBukkit = MythicBukkit.inst()

    override fun getEntityType(id: Key): EntityType {
        val mythicEntityType = MYTHIC_API.apiHelper.getMythicMob(id.value()).entityType
        return EntityType.valueOf(mythicEntityType.name)
    }

    override fun writeMobId(entity: Entity, id: Key) {
        entity.persistentDataContainer.set(MobKeys.TYPE, PersistentDataType.STRING, id.value())
    }
}