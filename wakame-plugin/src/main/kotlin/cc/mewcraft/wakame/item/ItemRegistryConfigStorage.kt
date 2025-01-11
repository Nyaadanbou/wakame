package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.core.RegistryConfigStorage
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.reloader.Reload

@Init(
    stage = InitStage.PRE_WORLD
)
@Reload
object ItemRegistryConfigStorage : RegistryConfigStorage {
    const val DIR_PATH = "items/"

}