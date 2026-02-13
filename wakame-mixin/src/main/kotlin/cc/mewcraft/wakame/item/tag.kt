package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.util.KoishKey
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.ApiStatus

interface KoishTagManagerApi {
    fun isTagged(itemStack: ItemStack, tagId: KoishKey): Boolean

    fun getValues(tagId: KoishKey): Set<ItemRef>

    companion object Impl : KoishTagManagerApi {

        private var implementation: KoishTagManagerApi = object : KoishTagManagerApi {
            override fun isTagged(itemStack: ItemStack, tagId: KoishKey): Boolean = false
            override fun getValues(tagId: KoishKey): Set<ItemRef> = emptySet()
        }

        @ApiStatus.Internal
        fun setImplementation(instance: KoishTagManagerApi) {
            this.implementation = instance
        }

        override fun isTagged(itemStack: ItemStack, tagId: KoishKey): Boolean {
            return implementation.isTagged(itemStack, tagId)
        }

        override fun getValues(tagId: KoishKey): Set<ItemRef> {
            return implementation.getValues(tagId)
        }
    }
}