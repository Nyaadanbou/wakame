package cc.mewcraft.wakame.enchantment2.metadata

import cc.mewcraft.wakame.ecs.bridge.FleksEntity
import com.github.quillraven.fleks.EntityComponentContext

/**
 * 负责从 [org.bukkit.enchantments.Enchantment] 上读取其 [net.minecraft.core.component.DataComponents],
 * 然后转化为 [com.github.quillraven.fleks.Component].
 *
 * FIXME #365: 也许不需要转换? 如果存在于 DataComponents 里的数据就是 fleks component
 *  那么当读取到 Enchantment 的时候, 直接 getEffects 就可以拿到上面的 component 了,
 *  然后便可以将其放入 fleks entity 上让 ecs 去运行.
 */
interface EnchantmentMeta<U, V> {

    fun make(effect: U): V

    context(EntityComponentContext)
    fun apply(entity: FleksEntity, value: V)

    context(EntityComponentContext)
    fun remove(entity: FleksEntity)

}