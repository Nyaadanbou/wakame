package cc.mewcraft.wakame.item.schema.cell.core.empty

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.item.binary.cell.core.BinaryCore
import cc.mewcraft.wakame.item.binary.cell.core.empty.BinaryEmptyCore
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.cell.core.SchemaCore
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * A constructor function to create [SchemaEmptyCore].
 */
fun SchemaEmptyCore(): SchemaEmptyCore {
    return SchemaEmptyCoreImpl
}

/**
 * Represents an empty [SchemaCore].
 */
interface SchemaEmptyCore : SchemaCore

//
// Internal Implementations
//

private data object SchemaEmptyCoreImpl : SchemaEmptyCore {
    override val key: Key = GenericKeys.EMPTY
    override fun reify(context: SchemaGenerationContext): BinaryCore {
        // Implementation notes:
        // There is nothing to populate into the context
        return BinaryEmptyCore()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key)
    )

    override fun toString(): String = toSimpleString()
}