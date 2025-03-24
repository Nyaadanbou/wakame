package cc.mewcraft.wakame.serialization.codec

import com.mojang.serialization.Codec
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.Block
import org.bukkit.Material
import org.bukkit.craftbukkit.block.CraftBlockType
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

    /**
     * 仅支持方块类型的 [Material].
     */
    @JvmField
    val MATERIAL_BLOCK: Codec<Material> = Block.CODEC.xmap(
        CraftBlockType<*>::minecraftToBukkit,
        CraftBlockType<*>::bukkitToMinecraft
    ).codec()

}