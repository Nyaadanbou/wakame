package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.NekoTags
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
        get() = base.tags.getCompound(NekoTags.Meta.ROOT)

    override val name: Component
        get() = gsonSerializer.deserialize(tags.getString(NekoTags.Meta.NAME))

    override val lore: List<Component>
        get() {
            val miniText = tags.getList(NekoTags.Meta.LORE, ShadowTagType.STRING)
            return if (miniText.size() == 0) {
                emptyList()
            } else {
                miniText.map {
                    gsonSerializer.deserialize((it as StringShadowTag).value())
                }
            }
        }

    override val level: Int?
        get() = tags.getIntOrNull(NekoTags.Meta.LEVEL)
    override val levelOrThrow: Int
        get() = level ?: throwIfNull()

    override val rarity: Rarity?
        get() {
            val byte = tags.getByteOrNull(NekoTags.Meta.RARITY)
                ?: return null
            return RarityRegistry.getBy(byte)
        }
    override val rarityOrThrow: Rarity
        get() = rarity ?: throwIfNull()

    override val element: Set<Element>
        get() {
            val byteArray = tags.getByteArrayOrNull(NekoTags.Meta.ELEMENT)
                ?: return emptySet()
            return byteArray.mapTo(
                ObjectArraySet(byteArray.size)
            ) {
                ElementRegistry.getByOrThrow(it)
            }
        }
    override val kizami: Set<Kizami>
        get() {
            val byteArray = tags.getByteArrayOrNull(NekoTags.Meta.KIZAMI)
                ?: return emptySet()
            return byteArray.mapTo(
                ObjectArraySet(byteArray.size)
            ) {
                KizamiRegistry.getByOrThrow(it)
            }
        }

    override val skin: ItemSkin?
        get() {
            val short = tags.getShortOrNull(NekoTags.Meta.SKIN)
                ?: return null
            return ItemSkinRegistry.getBy(short)
        }
    override val skinOrThrow: ItemSkin
        get() = skin ?: throwIfNull()
    override val skinOwner: UUID?
        get() {
            if (!tags.hasUUID(NekoTags.Meta.SKIN_OWNER))
                return null
            return tags.getUUID(NekoTags.Meta.SKIN_OWNER)
        }
    override val skinOwnerOrThrow: UUID
        get() = skinOwner ?: throwIfNull()

    ////// ItemMetaMapSetter //////

    private fun edit(consumer: CompoundShadowTag.() -> Unit) {
        tags.consumer()
    }

    override fun putRoot(compoundTag: CompoundShadowTag) {
        base.tags.put(NekoTags.Meta.ROOT, compoundTag)
    }

    override fun putName(name: Component) {
        val gsonString = gsonSerializer.serialize(name)
        edit { putString(NekoTags.Meta.NAME, gsonString) }
    }

    override fun putLore(lore: List<Component>) {
        val listBinaryTag = ListShadowTag.create(
            lore.map {
                val gsonString = gsonSerializer.serialize(it)
                StringShadowTag.valueOf(gsonString)
            },
            ShadowTagType.STRING
        )
        edit { put(NekoTags.Meta.LORE, listBinaryTag) }
    }

    override fun putLevel(level: Int?) {
        if (level == null) {
            edit { remove(NekoTags.Meta.LEVEL) }
        } else {
            edit { putByte(NekoTags.Meta.LEVEL, level.toStableByte()) }
        }
    }

    override fun putRarity(rarity: Rarity?) {
        if (rarity == null) {
            edit { remove(NekoTags.Meta.RARITY) }
        } else {
            edit { putByte(NekoTags.Meta.RARITY, rarity.binary) }
        }
    }

    override fun putElements(elements: Iterable<Element>) {
        val byteArray = elements.map { it.binary }.toByteArray()
        edit { putByteArray(NekoTags.Meta.ELEMENT, byteArray) }
    }

    override fun putKizami(kizami: Iterable<Kizami>) {
        val byteArray = kizami.map { it.binary }.toByteArray()
        edit { putByteArray(NekoTags.Meta.KIZAMI, byteArray) }
    }

    override fun putSkin(skin: ItemSkin?) {
        if (skin == null) {
            edit { remove(NekoTags.Meta.SKIN) }
        } else {
            edit { putShort(NekoTags.Meta.SKIN, skin.binary) }
        }
    }

    override fun putSkinOwner(skinOwner: UUID?) {
        if (skinOwner == null) {
            edit { remove(NekoTags.Meta.SKIN_OWNER) }
        } else {
            edit { putUUID(NekoTags.Meta.SKIN_OWNER, skinOwner) }
        }
    }

    private fun throwIfNull(): Nothing {
        throw NullPointerException("No such tag for item ${base.namespace}:${base.id}")
    }
}
