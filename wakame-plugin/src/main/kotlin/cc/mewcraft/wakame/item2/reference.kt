@file:JvmName("ItemReferenceSupport")

package cc.mewcraft.wakame.item2

import cc.mewcraft.wakame.item2.config.datagen.Context
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.util.Identifier
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Init(stage = InitStage.PRE_WORLD)
internal object ItemRefBootstrapper {

    @InitFun
    fun init() {
        // 注册内置的 ItemRefHandler 实例 ( 兜底的放在最后注册 )
        KoishRegistries2.INTERNAL_ITEM_REF_HANDLER.add("koish", KoishItemRefHandler)
        KoishRegistries2.INTERNAL_ITEM_REF_HANDLER.add("minecraft", MinecraftItemRefHandler)
        KoishRegistries2.INTERNAL_ITEM_REF_HANDLER.freeze()
    }

}

// ------------
// ItemRefHandler 的内置实现
// ------------

// 实现注意事项: 其他物品系统(如盲盒与酿酒)的实现应该放在 hooks 模块里

private data object MinecraftItemRefHandler : ItemRefHandler<Material> {

    override val systemName: String = "Minecraft"

    override fun supports(id: Identifier): Boolean {
        return Material.matchMaterial(id.asString()) != null
    }

    override fun getId(stack: ItemStack): Identifier? {
        return stack.type.key
    }

    override fun getName(id: Identifier): Component {
        val type = getInternalType(id)
        return Component.translatable(type)
    }

    override fun matches(xId: Identifier, yStack: ItemStack): Boolean {
        val yId = yStack.type.key
        return matches(xId, yId)
    }

    override fun createItemStack(id: Identifier, amount: Int, player: Player?): ItemStack {
        val type = getInternalType(id)
        return ItemStack(type).apply { this.amount = amount }
    }

    override fun getInternalType(id: Identifier): Material {
        return Material.matchMaterial(id.asString()) ?: throwItemTypeNotFound(id)
    }

}

// *原版套皮物品* 不属于此 handler 处理的范畴. 原版套皮物品依然是原版物品.
// 这样实现以符合 ItemRef API 的定义, 以及在所有场景下让实际表现符合预期.
private data object KoishItemRefHandler : ItemRefHandler<KoishItem> {

    override val systemName: String = "Koish"

    override fun supports(id: Identifier): Boolean {
        return KoishRegistries2.ITEM.containsId(id)
    }

    override fun getId(stack: ItemStack): Identifier? {
        // 对于 Koish 物品, 返回非空
        // 对于 原版套皮/纯原版/其他 物品, 返回空
        return stack.koishItem?.id
    }

    override fun getName(id: Identifier): Component {
        val type = getInternalType(id)
        return type.name
    }

    override fun matches(xId: Identifier, yStack: ItemStack): Boolean {
        // 对于 Koish 物品, 返回 true
        // 对于 原版套皮/纯原版/其他 物品, 返回 false
        val yId = yStack.koishItem?.id ?: return false
        return matches(xId, yId)
    }

    override fun createItemStack(id: Identifier, amount: Int, player: Player?): ItemStack {
        val type = getInternalType(id)
        val item = KoishStackGenerator.generate(type, Context())
        return item
    }

    override fun getInternalType(id: Identifier): KoishItem {
        return KoishRegistries2.ITEM[id] ?: throwItemTypeNotFound(id)
    }

}