package cc.mewcraft.wakame.item.datagen.impl

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.wakame.item.data.ItemDataTypes
import cc.mewcraft.wakame.item.datagen.ItemGenerationContext
import cc.mewcraft.wakame.item.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item.datagen.ItemMetaResult
import cc.mewcraft.wakame.item.datagen.impl.MetaSingleUseToken.TOKEN_LENGTH
import cc.mewcraft.wakame.util.MojangStack

/**
 * 一次性令牌的数据生成配置.
 *
 * 在物品生成时, 产生一串固定长度 ([TOKEN_LENGTH]) 的随机字母数字令牌, 并写入物品堆叠.
 *
 * 该令牌用于确保每个物品的唯一性, 配合 [cc.mewcraft.wakame.item.token.SingleUseTokenRepository] 防止物品被重复消耗.
 */
object MetaSingleUseToken : ItemMetaEntry<String> {

    const val TOKEN_LENGTH = 16

    @JvmField
    val SERIALIZER: SimpleSerializer<MetaSingleUseToken> = SimpleSerializer { _, _ -> MetaSingleUseToken }

    private val CHARS = ('A'..'Z') + ('a'..'z') + ('0'..'9')

    override fun randomized(): Boolean {
        return true
    }

    override fun make(context: ItemGenerationContext): ItemMetaResult<String> {
        val token = buildString(TOKEN_LENGTH) {
            repeat(TOKEN_LENGTH) {
                append(CHARS.random())
            }
        }
        return ItemMetaResult.of(token)
    }

    override fun write(value: String, itemstack: MojangStack) {
        itemstack.ensureSetData(ItemDataTypes.SINGLE_USE_TOKEN, value)
    }
}
