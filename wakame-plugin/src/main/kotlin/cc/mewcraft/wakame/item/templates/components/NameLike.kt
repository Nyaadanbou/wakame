package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.template.ItemGenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

private val MM = MiniMessage.miniMessage()

abstract class NameLike : ItemTemplate<Component> {
    abstract val plainName: String // 纯文本
    abstract val fancyName: String // MM格式

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<Component> {
        // 开发日记 2024/6/29
        // 根据设计, custom_name 和 item_name 都不经过发包系统处理.
        // 因此, 生成 custom_name 的时候就需要把 CustomName.rich
        // 根据上下文也生成好, 不能写 Component.empty.

        val resolver = TagResolver.builder().apply {
            val rarity = context.rarity
            if (rarity != null) {
                tag("rarity") { queue: ArgumentQueue, ctx: Context ->
                    val arg = queue.popOr("Tag <rarity:_> must have an argument. Available arguments: 'name', 'style'").lowerValue()
                    when (arg) {
                        "name" -> Tag.selfClosingInserting(rarity.value.displayName)
                        "style" -> Tag.styling(*rarity.value.displayStyles)
                        else -> throw ctx.newException("Unknown argument. Available arguments: 'name', 'style'", queue)
                    }
                }
            }
        }.build()

        val itemName = MM.deserialize(fancyName, resolver)

        return ItemGenerationResult.of(itemName)
    }
}