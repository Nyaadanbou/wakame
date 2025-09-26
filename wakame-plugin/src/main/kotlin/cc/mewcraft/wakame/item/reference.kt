@file:JvmName("ItemReferenceSupport")

package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.item.datagen.ItemGenerationContext
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.KOISH_NAMESPACE
import cc.mewcraft.wakame.util.MINECRAFT_NAMESPACE
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Init(stage = InitStage.PRE_WORLD)
internal object ItemRefBootstrap {

    @InitFun
    fun init() {
        // 注册内置的 ItemRefHandler 实例 ( 兜底的放在最后注册 )
        BuiltInRegistries.ITEM_REF_HANDLER_INTERNAL.add(KOISH_NAMESPACE, KoishItemRefHandler)
        BuiltInRegistries.ITEM_REF_HANDLER_INTERNAL.add(MINECRAFT_NAMESPACE, MinecraftItemRefHandler)
        BuiltInRegistries.ITEM_REF_HANDLER_INTERNAL.freeze()
    }

}

// ------------
// ItemRefHandler 的内置实现
// ------------

// 实现注意事项: 其他物品系统(如盲盒与酿酒)的代码实现应该放在 hooks 模块里

/*private*/ data object MinecraftItemRefHandler : ItemRefHandler<Material> {

    override val systemName: String = "Minecraft"

    override fun accepts(id: Identifier): Boolean {
        return Material.matchMaterial(id.asString()) != null
    }

    override fun getId(stack: ItemStack): Identifier? {
        return stack.type.key
    }

    override fun getName(id: Identifier): Component? {
        val type = getInternalType(id) ?: return null
        return Component.translatable(type)
    }

    override fun createItemStack(id: Identifier, amount: Int, player: Player?): ItemStack? {
        val type = getInternalType(id) ?: return null
        return ItemStack(type).apply { this.amount = amount }
    }

    override fun getInternalType(id: Identifier): Material? {
        return Material.matchMaterial(id.asString())
    }

}

// *原版套皮物品* 不属于此 handler 处理的范畴. 原版套皮物品依然是原版物品.
// 这样实现以符合 ItemRef API 的定义, 以及在所有场景下让实际表现符合预期.
//
// make it public so that other item systems can make use of it
/*private*/ data object KoishItemRefHandler : ItemRefHandler<KoishItem> {

    override val systemName: String = "Koish"

    override fun accepts(id: Identifier): Boolean {
        return BuiltInRegistries.ITEM.containsId(id)
    }

    override fun getId(stack: ItemStack): Identifier? {
        // 对于 Koish 物品, 返回非空
        // 对于 原版套皮/纯原版/其他 物品, 返回空
        return stack.koishTypeId
    }

    override fun getName(id: Identifier): Component? {
        val type = getInternalType(id) ?: return null
        return type.name
    }

    override fun createItemStack(id: Identifier, amount: Int, player: Player?): ItemStack? {
        val type = getInternalType(id) ?: return null
        val item = KoishStackGenerator.generate(type, ItemGenerationContext(type, 0f, 0))
        return item
    }

    override fun getInternalType(id: Identifier): KoishItem? {
        return BuiltInRegistries.ITEM[id]
    }

}