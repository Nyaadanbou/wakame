package cc.mewcraft.wakame.entity.attribute

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.entity.typeref.EntityRefLookup
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.util.registerEvents
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.world.EntitiesLoadEvent
import org.bukkit.event.world.EntitiesUnloadEvent
import org.jetbrains.annotations.ApiStatus
import java.util.*

object AttributeMapPatches : Listener {

    private val entityRefLookup: EntityRefLookup by Injector.inject()
    private val uuidToPatch = Object2ObjectOpenHashMap<UUID, AttributeMapPatch>()

    fun get(attributable: UUID): AttributeMapPatch? {
        return uuidToPatch[attributable]
    }

    fun getOrCreate(attributable: UUID): AttributeMapPatch {
        return uuidToPatch.computeIfAbsent(attributable, Object2ObjectFunction { AttributeMapPatch() })
    }

    fun put(attributable: UUID, patch: AttributeMapPatch) {
        uuidToPatch[attributable] = patch
    }

    fun remove(attributable: UUID) {
        uuidToPatch.remove(attributable)
    }

    @ApiStatus.Internal
    fun init() {
        registerEvents()
    }

    @ApiStatus.Internal
    fun close() {
        SERVER.worlds.flatMap(World::getEntities).forEach(::onEntityUnload)
    }

    // 当实体加载时, 读取 PDC 中的 AttributeMapPatch
    @EventHandler
    private fun on(event: EntitiesLoadEvent) {
        for (entity in event.entities) {
            if (entity is Player) continue
            if (entity !is LivingEntity) continue

            val patch = AttributeMapPatch.Constants.decode(entity)

            put(entity.uniqueId, patch)

            // 触发 AttributeMap 的初始化, 例如应用原版属性
            AttributeMapAccess.INSTANCE.get(entity).onFailure {
                LOGGER.error("Failed to initialize the attribute map for entity ${entity}: ${it.message}")
            }
        }
    }

    @EventHandler
    private fun on(event: CreatureSpawnEvent) {
        // Note: 玩家在世界中的生成不会触发 CreaturesSpawnEvent

        // 触发 AttributeMap 的初始化, 例如应用原版属性
        val entity = event.entity
        val attributeMap = AttributeMapAccess.INSTANCE.get(entity).getOrElse {
            LOGGER.error("Failed to initialize the attribute map for entity $entity: ${it.message}")
            return
        }

        // 将生物血量设置到最大血量
        val maxHealth = attributeMap.getValue(Attributes.MAX_HEALTH)
        entity.health = maxHealth
    }

    // 当实体卸载时, 将 AttributeMapPatch 保存到 PDC
    @EventHandler
    private fun on(event: EntitiesUnloadEvent) {
        for (entity in event.entities) {
            if (entity is Player) continue
            if (entity !is LivingEntity) continue

            val patch = get(entity.uniqueId) ?: continue
            val default = KoishRegistries.ATTRIBUTE_SUPPLIER.getOrThrow(entityRefLookup.get(entity))

            // 把跟默认属性一样的属性移除
            patch.trimBy(default)

            // 把原版属性移除, 这部分由 nms 自己处理
            patch.removeIf { attribute, _ -> !attribute.vanilla }

            // 如果经过以上操作后的 patch 为空, 表示生物并没有 patch, 移除 PDC
            if (patch.isEmpty()) {
                patch.removeFrom(entity)
                remove(entity.uniqueId)
                continue
            }

            patch.saveTo(entity)

            remove(entity.uniqueId)
        }
    }

    private fun onEntityUnload(entity: Entity) {
        if (entity is Player) return
        if (entity !is LivingEntity) return

        val patch = get(entity.uniqueId) ?: return
        val default = KoishRegistries.ATTRIBUTE_SUPPLIER.getOrThrow(entityRefLookup.get(entity))

        // 把跟默认属性一样的属性移除
        patch.trimBy(default)

        // 把原版属性移除, 这部分由 nms 自己处理
        patch.removeIf { attribute, _ -> !attribute.vanilla }

        // 如果经过以上操作后的 patch 为空, 表示生物并没有 patch, 移除 PDC
        if (patch.isEmpty()) {
            patch.removeFrom(entity)
            remove(entity.uniqueId)
            return
        }

        patch.saveTo(entity)

        remove(entity.uniqueId)
    }
}