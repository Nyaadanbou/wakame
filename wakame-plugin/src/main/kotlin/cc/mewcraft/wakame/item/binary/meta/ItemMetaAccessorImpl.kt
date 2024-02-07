package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.binary.WakaItemStackImpl
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.registry.*
import cc.mewcraft.wakame.skin.ItemSkin
import cc.mewcraft.wakame.util.*
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ListShadowTag
import me.lucko.helper.shadows.nbt.StringShadowTag
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

@OptIn(InternalApi::class)
internal class ItemMetaAccessorImpl(
    private val base: WakaItemStackImpl,
) : KoinComponent, ItemMetaAccessor {
    private val gsonSerializer: GsonComponentSerializer by inject()

    ////// ItemMetaMap //////

    override val tags: CompoundShadowTag
        get() = base.tags.getCompound(ItemMetaTagNames.ROOT)

    override val name: Component
        get() = gsonSerializer.deserialize(tags.getString(ItemMetaTagNames.NAME))

    override val lore: List<Component>
        get() {
            val miniText = tags.getList(ItemMetaTagNames.LORE, ShadowTagType.STRING)
            return if (miniText.size() == 0) {
                emptyList()
            } else {
                miniText.map {
                    gsonSerializer.deserialize((it as StringShadowTag).value())
                }
            }
        }

    override val level: Int?
        get() = tags.getIntOrNull(ItemMetaTagNames.LEVEL)
    override val levelOrThrow: Int
        get() = level ?: throwIfNull()

    override val rarity: Rarity?
        get() = RarityRegistry.get(
            RarityRegistry.getNameBy(
                tags.getByteOrNull(ItemMetaTagNames.RARITY)
            )
        )
    override val rarityOrThrow: Rarity
        get() = rarity ?: throwIfNull()

    override val elements: Set<Element>
        get() {
            val byteArray = tags.getByteArrayOrNull(ItemMetaTagNames.ELEMENTS)
                ?: return emptySet()
            val ret = ObjectArraySet<Element>(byteArray.size)
            byteArray.map {
                ElementRegistry.getBy(it)
            }.filterNotNullTo(ret)
            return ret
        }

    override val kizami: Set<Kizami>
        get() {
            val byteArray = tags.getByteArrayOrNull(ItemMetaTagNames.KIZAMI)
                ?: return emptySet()
            val ret = ObjectArraySet<Kizami>(byteArray.size)
            byteArray.map {
                KizamiRegistry.getBy(it)
            }.filterNotNullTo(ret)
            return ret
        }

    override val skin: ItemSkin?
        get() {
            val short = tags.getShortOrNull(ItemMetaTagNames.SKIN)
                ?: return null
            return ItemSkinRegistry.getBy(short)
        }
    override val skinOrThrow: ItemSkin
        get() = skin ?: throwIfNull()

    override val skinOwner: UUID?
        get() {
            if (!tags.hasUUID(ItemMetaTagNames.SKIN_OWNER)) {
                return null
            }
            return tags.getUUID(ItemMetaTagNames.SKIN_OWNER)
        }
    override val skinOwnerOrThrow: UUID
        get() = skinOwner ?: throwIfNull()

    ////// ItemMetaMapSetter //////

    private fun edit(consumer: CompoundShadowTag.() -> Unit) {
        tags.consumer()
    }

    override fun putRoot(compoundTag: CompoundShadowTag) {
        base.tags.put(ItemMetaTagNames.ROOT, compoundTag)
    }

    override fun putName(name: Component) {
        val gsonString = gsonSerializer.serialize(name)
        edit { putString(ItemMetaTagNames.NAME, gsonString) }
    }

    override fun putLore(lore: List<Component>) {
        val listBinaryTag = ListShadowTag.create(
            lore.map {
                val gsonString = gsonSerializer.serialize(it)
                StringShadowTag.valueOf(gsonString)
            },
            ShadowTagType.STRING
        )
        edit { put(ItemMetaTagNames.LORE, listBinaryTag) }
    }

    override fun putLevel(level: Int?) {
        if (level == null) {
            edit { remove(ItemMetaTagNames.LEVEL) }
        } else {
            edit { putByte(ItemMetaTagNames.LEVEL, level.toStableByte()) }
        }
    }

    override fun putRarity(rarity: Rarity?) {
        if (rarity == null) {
            edit { remove(ItemMetaTagNames.RARITY) }
        } else {
            edit { putByte(ItemMetaTagNames.RARITY, rarity.binary) }
        }
    }

    override fun putElements(elements: Iterable<Element>) {
        val byteArray = elements.map { it.binary }.toByteArray()
        edit { putByteArray(ItemMetaTagNames.ELEMENTS, byteArray) }
    }

    override fun putKizami(kizami: Iterable<Kizami>) {
        val byteArray = kizami.map { it.binary }.toByteArray()
        edit { putByteArray(ItemMetaTagNames.KIZAMI, byteArray) }
    }

    override fun putSkin(skin: ItemSkin?) {
        if (skin == null) {
            edit { remove(ItemMetaTagNames.SKIN) }
        } else {
            edit { putShort(ItemMetaTagNames.SKIN, skin.binary) }
        }
    }

    override fun putSkinOwner(skinOwner: UUID?) {
        if (skinOwner == null) {
            edit { remove(ItemMetaTagNames.SKIN_OWNER) }
        } else {
            edit { putUUID(ItemMetaTagNames.SKIN_OWNER, skinOwner) }
        }
    }

    private fun throwIfNull(): Nothing {
        throw NullPointerException("No such tag for item ${base.namespace}:${base.id}")
    }
}
