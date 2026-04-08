package cc.mewcraft.wakame.item.property.impl

import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class VirtualBook(
    val title: Component,
    val author: Component,
    val pages: List<Component>,
) {
    fun createAdventureBook(): Book {
        return Book.book(title, author, pages)
    }
}