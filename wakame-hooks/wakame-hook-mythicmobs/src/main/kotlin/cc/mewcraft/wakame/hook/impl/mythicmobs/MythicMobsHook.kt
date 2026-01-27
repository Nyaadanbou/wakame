package cc.mewcraft.wakame.hook.impl.mythicmobs

import cc.mewcraft.wakame.hook.impl.mythicmobs.listener.ConfigListener
import cc.mewcraft.wakame.hook.impl.mythicmobs.listener.DamageListener
import cc.mewcraft.wakame.hook.impl.mythicmobs.listener.ReloadListener
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.skill.SkillIntegration
import cc.mewcraft.wakame.mixin.support.MythicPluginBridge
import cc.mewcraft.wakame.util.registerEvents
import io.lumine.mythic.core.constants.MobKeys
import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity
import org.bukkit.persistence.PersistentDataType

@Hook(plugins = ["MythicMobs"])
object MythicMobsHook {
    init {
        // 注册 Listeners
        ConfigListener.registerEvents()
        DamageListener.registerEvents()
        ReloadListener.registerEvents()

        MythicPluginBridge.setImplementation(MythicPluginBridgeImpl)
        SkillIntegration.setImplementation(MythicSkillIntegration)

        // 目前的所有实现暂时不需要获取 MythicMobs 的怪物的 id, 等之后需要的时候再把这个注释给去掉.
        // BuiltInRegistries.ENTITY_REF_LOOKUP_DIR.add("mythicmobs", MythicMobsEntityRefLookupDictionary())
    }
}

private object MythicPluginBridgeImpl : MythicPluginBridge {

    override fun writeIdMark(entity: Entity, id: Key) {
        entity.persistentDataContainer.set(MobKeys.TYPE, PersistentDataType.STRING, id.value())
    }
}