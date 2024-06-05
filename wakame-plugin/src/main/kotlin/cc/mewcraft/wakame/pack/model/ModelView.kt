package cc.mewcraft.wakame.pack.model

import java.util.UUID

interface ModelView {
    val uniqueId: UUID

    fun playAnimation(model: Model, animation: String)

    fun stopAnimation(model: Model, animation: String)
}