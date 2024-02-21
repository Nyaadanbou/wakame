package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.attribute.base.AttributeModifier
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.registry.KizamiRegistry
import cc.mewcraft.wakame.registry.RarityRegistry
import cc.mewcraft.wakame.registry.getByOrThrow
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import me.lucko.helper.shadows.nbt.CompoundShadowTag

/* Custom Tag Extensions */

//<editor-fold desc="Rarity Getters/Setters">
/**
 * Gets the stored [rarity][Rarity] from the specified tag.
 *
 * Returns the [default][RarityRegistry.DEFAULT] if the tag does not exist.
 *
 * @throws IllegalArgumentException if the stored rarity is not registered
 */
internal fun CompoundShadowTag.getRarity(key: String): Rarity {
    val byte = getByteOrNull(key)
        ?: return RarityRegistry.DEFAULT
    return RarityRegistry.getByOrThrow(byte)
}
//</editor-fold>


//<editor-fold desc="Element Getters/Setters">
/**
 * Gets the stored [element][Element] from the specified **Byte** tag.
 *
 * Returns the [default][ElementRegistry.DEFAULT] if the tag does not
 * exist.
 *
 * @throws IllegalArgumentException if the stored element is not registered
 */
internal fun CompoundShadowTag.getElement(key: String): Element {
    val byte = getByteOrNull(key)
        ?: return ElementRegistry.DEFAULT
    return ElementRegistry.getByOrThrow(byte)
}

/**
 * Gets all [elements][Element] from the specified **Byte Array** tag.
 *
 * Returns empty set if the specified tag does not exist.
 *
 * @throws IllegalArgumentException if any of the stored element is not
 *     registered
 */
internal fun CompoundShadowTag.getElementSet(key: String): Set<Element> {
    val byteArray = getByteArrayOrNull(key)
        ?: return emptySet()
    return byteArray.mapTo(
        ObjectArraySet(byteArray.size)
    ) {
        ElementRegistry.getByOrThrow(it)
    }
}

internal fun CompoundShadowTag.putElement(key: String, element: Element) {
    putByte(key, element.binary)
}

internal fun CompoundShadowTag.putElement(key: String, elementCollection: Collection<Element>) {
    val elementByteArray = ByteArray(elementCollection.size)
    var i = 0
    for (element in elementCollection) {
        elementByteArray[i++] = element.binary
    }
    putByteArray(key, elementByteArray)
}
//</editor-fold>

//<editor-fold desc="Kizami Getters/Setters">
/**
 * Gets all [kizami][Kizami] from the specified **Byte Array** tag.
 *
 * Returns an empty set if the specified tag does not exist.
 *
 * @throws IllegalArgumentException if any of the stored element is not
 *     registered
 */
internal fun CompoundShadowTag.getKizamiSet(key: String): Set<Kizami> {
    val byteArray = getByteArrayOrNull(key)
        ?: return emptySet()
    return byteArray.mapTo(
        ObjectArraySet(byteArray.size)
    ) {
        KizamiRegistry.getByOrThrow(it)
    }
}

internal fun CompoundShadowTag.putKizami(key: String, kizami: Kizami) {
    putByte(key, kizami.binary)
}

internal fun CompoundShadowTag.putKizami(key: String, kizamiCollection: Collection<Kizami>) {
    val kizamiByteArray = ByteArray(kizamiCollection.size)
    var i = 0
    for (kizami in kizamiCollection) {
        kizamiByteArray[i++] = kizami.binary
    }
    putByteArray(key, kizamiByteArray)
}
//</editor-fold>

//<editor-fold desc="Operation Getters/Setters">
internal fun CompoundShadowTag.getOperation(key: String): AttributeModifier.Operation {
    return AttributeModifier.Operation.byId(getInt(key))
}

internal fun CompoundShadowTag.putOperation(key: String, operation: AttributeModifier.Operation) {
    putByte(key, operation.binary)
}
//</editor-fold>
