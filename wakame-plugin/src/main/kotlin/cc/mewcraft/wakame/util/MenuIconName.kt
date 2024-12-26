package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.Injector
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class MenuIconName(
    @Setting(nodeFromParent = true)
    val name: String,
) {
    companion object {
        private val MM = Injector.get<MiniMessage>()
    }

    fun resolve(tagResolver: TagResolver): Component {
        return MM.deserialize(name, tagResolver)
    }

    fun resolve(dict: MenuIconDictionary = MenuIconDictionary(), dsl: MenuIconLore.PlaceholderTagResolverBuilder.() -> Unit): Component {
        return MM.deserialize(name, MenuIconLore.PlaceholderTagResolverBuilder(dict).apply(dsl).build())
    }
}