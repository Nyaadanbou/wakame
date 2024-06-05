package cc.mewcraft.wakame.pack.model

import org.bukkit.entity.Entity
import java.io.File

interface WakameModelEngine {
    fun loadModel(file: File, cursor: Int = 0): Model

    fun spawn(model: Model, baseEntity: Entity): ModelView
}