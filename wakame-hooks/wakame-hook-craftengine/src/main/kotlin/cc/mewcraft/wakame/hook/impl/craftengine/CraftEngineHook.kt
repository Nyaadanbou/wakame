package cc.mewcraft.wakame.hook.impl.craftengine

import cc.mewcraft.wakame.integration.Hook
import net.momirealms.craftengine.bukkit.api.BukkitAdaptors
import net.momirealms.craftengine.core.entity.furniture.FurnitureExtraData
import net.momirealms.craftengine.core.plugin.CraftEngine
import net.momirealms.craftengine.core.util.Key
import net.momirealms.craftengine.core.world.WorldPosition
import net.momirealms.craftengine.libraries.nbt.CompoundTag
import org.bukkit.Location
import kotlin.jvm.optionals.getOrNull

@Hook(plugins = ["CraftEngine"])
object CraftEngineHook {

    // TODO Koish 需要有一套基本的家具接口

    fun test(loc: Location) {
        val furniture = CraftEngine.instance().furnitureManager().furnitureById(Key.of("furniture", "chair")).getOrNull() ?: return
        val worldPos = WorldPosition(BukkitAdaptors.adapt(loc.world), loc.x, loc.y, loc.z)
        val extraData = FurnitureExtraData(CompoundTag())
        val playSound = true
        CraftEngine.instance().furnitureManager().place(worldPos, furniture, extraData, playSound)
    }
}