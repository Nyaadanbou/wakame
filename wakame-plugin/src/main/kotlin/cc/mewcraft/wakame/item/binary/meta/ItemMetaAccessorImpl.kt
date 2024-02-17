package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.binary.NekoItemStackImpl
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

internal class ItemMetaAccessorImpl(
    private val base: NekoItemStackImpl,
) : KoinComponent, ItemMetaAccessor {
    private val gsonSerializer: GsonComponentSerializer by inject(mode = LazyThreadSafetyMode.NONE)

    ////// ItemMetaMap //////

    private val rootOrNull: CompoundShadowTag?
        get() = base.tags.getCompoundOrNull(NekoTags.Meta.ROOT)
    private val rootOrCreate: CompoundShadowTag
        get() = base.tags.getOrPut(NekoTags.Meta.ROOT, CompoundShadowTag::create)

    override val name: String?
        get() = rootOrNull
            ?.getStringOrNull(NekoTags.Meta.NAME)
    override val nameOrEmpty: String
        get() = name.orEmpty()

    override val lore: List<String>?
        get() = rootOrNull
            ?.getListOrNull(NekoTags.Meta.LORE, ShadowTagType.STRING)
            ?.map { (it as StringShadowTag).value() }
    override val loreOrEmpty: List<String>
        get() = lore.orEmpty()

    override val level: Int?
        get() = rootOrNull?.getIntOrNull(NekoTags.Meta.LEVEL)
    override val levelOrThrow: Int
        get() = level ?: throwIfNull()

    override val rarity: Rarity?
        get() = rootOrNull
            ?.getByteOrNull(NekoTags.Meta.RARITY)
            ?.let { RarityRegistry.getBy(it) }
    override val rarityOrThrow: Rarity
        get() = rarity ?: throwIfNull()

    override val element: Set<Element>?
        get() = rootOrNull
            ?.getByteArrayOrNull(NekoTags.Meta.ELEMENT)
            ?.mapTo(ObjectArraySet(4)) { ElementRegistry.getByOrThrow(it) }
    override val elementOrEmpty: Set<Element>
        get() = element ?: emptySet()

    override val kizami: Set<Kizami>?
        get() = rootOrNull
            ?.getByteArrayOrNull(NekoTags.Meta.KIZAMI)
            ?.mapTo(ObjectArraySet(4)) { KizamiRegistry.getByOrThrow(it) }
    override val kizamiOrEmpty: Set<Kizami>
        get() = kizami ?: emptySet()

    override val skin: ItemSkin?
        get() = rootOrNull
            ?.getShortOrNull(NekoTags.Meta.SKIN)
            ?.let { ItemSkinRegistry.getBy(it) }
    override val skinOrThrow: ItemSkin
        get() = skin ?: throwIfNull()

    override val skinOwner: UUID?
        get() {
            val rootOrNull = rootOrNull
            if (rootOrNull == null || !rootOrNull.hasUUID(NekoTags.Meta.SKIN_OWNER))
                return null
            return rootOrNull.getUUID(NekoTags.Meta.SKIN_OWNER)
        }
    override val skinOwnerOrThrow: UUID
        get() = skinOwner ?: throwIfNull()

    ////// ItemMetaMapSetter //////

    override fun putRoot(compoundTag: CompoundShadowTag) {
        base.tags.put(NekoTags.Meta.ROOT, compoundTag)
    }

    override fun putName(name: Component) {
        gsonSerializer.serialize(name).let { rootOrCreate.putString(NekoTags.Meta.NAME, it) }
    }

    override fun putLore(lore: List<Component>) {
        val listBinaryTag = ListShadowTag.create(
            lore.map {
                val gsonString = gsonSerializer.serialize(it)
                StringShadowTag.valueOf(gsonString)
            },
            ShadowTagType.STRING
        )
        rootOrCreate.put(NekoTags.Meta.LORE, listBinaryTag)
    }

    override fun putLevel(level: Int?) {
        if (level == null) {
            rootOrNull?.remove(NekoTags.Meta.LEVEL)
        } else {
            rootOrCreate.putByte(NekoTags.Meta.LEVEL, level.toStableByte())
        }
    }

    override fun putRarity(rarity: Rarity?) {
        if (rarity == null) {
            rootOrNull?.remove(NekoTags.Meta.RARITY)
        } else {
            rootOrCreate.putByte(NekoTags.Meta.RARITY, rarity.binary)
        }
    }

    override fun putElements(elements: Collection<Element>) {
        val byteArray = elements.map { it.binary }.toByteArray()
        if (byteArray.isEmpty()) {
            rootOrNull?.remove(NekoTags.Meta.ELEMENT)
        } else {
            rootOrCreate.putByteArray(NekoTags.Meta.ELEMENT, byteArray)
        }
    }

    override fun putKizami(kizami: Collection<Kizami>) {
        val byteArray = kizami.map { it.binary }.toByteArray()
        if (byteArray.isEmpty()) {
            rootOrNull?.remove(NekoTags.Meta.KIZAMI)
        } else {
            rootOrCreate.putByteArray(NekoTags.Meta.KIZAMI, byteArray)
        }
    }

    override fun putSkin(skin: ItemSkin?) {
        if (skin == null) {
            rootOrNull?.remove(NekoTags.Meta.SKIN)
        } else {
            rootOrCreate.putShort(NekoTags.Meta.SKIN, skin.binary)
        }
    }

    override fun putSkinOwner(skinOwner: UUID?) {
        if (skinOwner == null) {
            rootOrNull?.remove(NekoTags.Meta.SKIN_OWNER)
        } else {
            rootOrCreate.putUUID(NekoTags.Meta.SKIN_OWNER, skinOwner)
        }
    }

    private fun throwIfNull(): Nothing {
        throw NullPointerException("No such tag for item ${base.namespace}:${base.id}")
    }
}
