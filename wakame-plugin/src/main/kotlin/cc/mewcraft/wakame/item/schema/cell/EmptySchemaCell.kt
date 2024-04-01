package cc.mewcraft.wakame.item.schema.cell

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.core.SchemaCore
import cc.mewcraft.wakame.item.schema.curse.SchemaCurse
import cc.mewcraft.wakame.random.Group

@InternalApi
internal object EmptySchemaCell : SchemaCell {
    override val canReforge: Boolean = false
    override val canOverride: Boolean = false
    override val keepEmpty: Boolean = false
    override val coreSelector: Group<SchemaCore, SchemaGenerationContext> = Group.empty()
    override val curseSelector: Group<SchemaCurse, SchemaGenerationContext> = Group.empty()
}