package cc.mewcraft.wakame.display2

import cc.mewcraft.wakame.display.RENDERERS_CONFIG_DIR
import cc.mewcraft.wakame.display.StandardItemRenderer
import cc.mewcraft.wakame.packet.PacketNekoStack
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import kotlin.io.path.Path

internal object RendererSystems {
    private val SYSTEMS: BiMap<RendererSystemName, RendererSystem<*>> = HashBiMap.create()

    val STANDARD: RendererSystem<PacketNekoStack> = register(RendererSystemName.STANDARD) { RendererSystem.create(Path("$RENDERERS_CONFIG_DIR/standard/layout.yml")) { StandardItemRenderer(it) } }

    private fun <T> register(name: RendererSystemName, builder: () -> RendererSystem<T>): RendererSystem<T> {
        return builder().also { SYSTEMS[name] = it }
    }

    operator fun get(name: RendererSystemName): RendererSystem<*> {
        return SYSTEMS[name] ?: throw IllegalArgumentException("Unknown renderer system: $name")
    }

    fun getRendererSystemName(rendererSystem: RendererSystem<*>): RendererSystemName {
        return SYSTEMS.inverse()[rendererSystem] ?: throw IllegalArgumentException("Unknown renderer system: $rendererSystem")
    }
    
    fun entries(): Set<Map.Entry<RendererSystemName, RendererSystem<*>>> {
        return SYSTEMS.entries
    }
}

enum class RendererSystemName {
    STANDARD
}