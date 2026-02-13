package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.util.Identifier
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.ApiStatus

interface KoishTagManagerApi {
    fun isTagged(itemStack: ItemStack, tagId: Identifier): Boolean

    fun getValues(tagId: Identifier): Set<ItemRef>

    companion object Impl : KoishTagManagerApi {

        private var implementation: KoishTagManagerApi = object : KoishTagManagerApi {
            override fun isTagged(itemStack: ItemStack, tagId: Identifier): Boolean = false
            override fun getValues(tagId: Identifier): Set<ItemRef> = emptySet()
        }

        @ApiStatus.Internal
        fun setImplementation(instance: KoishTagManagerApi) {
            this.implementation = instance
        }

        override fun isTagged(itemStack: ItemStack, tagId: Identifier): Boolean {
            return implementation.isTagged(itemStack, tagId)
        }

        override fun getValues(tagId: Identifier): Set<ItemRef> {
            return implementation.getValues(tagId)
        }
    }
}