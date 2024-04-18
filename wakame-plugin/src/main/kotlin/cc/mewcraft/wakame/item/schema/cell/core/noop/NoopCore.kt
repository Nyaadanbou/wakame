package cc.mewcraft.wakame.item.schema.cell.core.noop

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.item.binary.cell.core.noop.BinaryNoopCore
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.cell.core.SchemaCore
import net.kyori.adventure.key.Key

/**
 * A constructor function to create [SchemaNoopCore].
 */
fun SchemaNoopCore(): SchemaNoopCore {
    return SchemaNoopCoreImpl
}

/**
 * Represents a noop [SchemaCore].
 *
 * By design, an [SchemaNoopCore] is a "mark" core which is used to
 * indicate that it should never be written to the final item NBT.
 */
interface SchemaNoopCore : SchemaCore

//
// Internal Implementations
//

private data object SchemaNoopCoreImpl : SchemaNoopCore {
    override val key: Key = GenericKeys.NOOP
    override fun reify(context: SchemaGenerationContext) = BinaryNoopCore()
}