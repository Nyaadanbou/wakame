package cc.mewcraft.wakame.serialization.codec

import com.mojang.serialization.Codec
import net.minecraft.core.registries.BuiltInRegistries
import org.bukkit.Material
import org.bukkit.craftbukkit.inventory.CraftItemType

/**
 * Bukkit API 数据类型的 Codec.
 */
object BukkitCodecs {

    @JvmField
    val MATERIAL: Codec<Material> = BuiltInRegistries.ITEM.byNameCodec().xmap(
        CraftItemType<*>::minecraftToBukkit,
        CraftItemType<*>::bukkitToMinecraft
    )

}