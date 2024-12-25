package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.Injector
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class MenuIconName(
    val name: String,
) {
    companion object {
        private val MM = Injector.get<MiniMessage>()
    }

    fun resolve(dict: MenuIconDictionary = MenuIconDictionary(), dsl: MenuIconLore.PlaceholderTagResolverBuilder.() -> Unit): Component {
        return MM.deserialize(name, MenuIconLore.PlaceholderTagResolverBuilder(dict).apply(dsl).build())
    }
}