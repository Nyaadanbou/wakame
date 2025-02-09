package cc.mewcraft.wakame.util.bossbar

import cc.mewcraft.wakame.network.ClientboundBossEventPacket
import cc.mewcraft.wakame.util.bossbar.operation.*
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.game.ClientboundBossEventPacket
import net.minecraft.world.BossEvent
import java.util.*

class BossBar(
    val id: UUID,
    name: Component = Component.text(""),
    progress: Float = 0.0f,
    color: BossEvent.BossBarColor = BossEvent.BossBarColor.WHITE,
    overlay: BossEvent.BossBarOverlay = BossEvent.BossBarOverlay.PROGRESS,
    darkenScreen: Boolean = false,
    playMusic: Boolean = false,
    createWorldFog: Boolean = false
) {
    
    var name: Component = name
        set(value) {
            field = value
            
            _addOperation = null
            _updateNameOperation = null
            
            _addPacket = null
            _updateNamePacket = null
        }
    var progress: Float = progress
        set(value) {
            field = value
            
            _addOperation = null
            _updateProgressOperation = null
            
            _addPacket = null
            _updateProgressPacket = null
        }
    var color: BossEvent.BossBarColor = color
        set(value) {
            field = value
            
            _addOperation = null
            _updateStyleOperation = null
            
            _addPacket = null
            _updateStylePacket = null
        }
    var overlay: BossEvent.BossBarOverlay = overlay
        set(value) {
            field = value
            
            _addOperation = null
            _updateStyleOperation = null
            
            _addPacket = null
            _updateStylePacket = null
        }
    var darkenScreen: Boolean = darkenScreen
        set(value) {
            field = value
            
            _addOperation = null
            _updatePropertiesOperation = null
            
            _addPacket = null
            _updatePropertiesPacket = null
        }
    var playMusic: Boolean = playMusic
        set(value) {
            field = value
            
            _addOperation = null
            _updatePropertiesOperation = null
            
            _addPacket = null
            _updatePropertiesPacket = null
        }
    var createWorldFog: Boolean = createWorldFog
        set(value) {
            field = value
            
            _addOperation = null
            _updatePropertiesOperation = null
            
            _addPacket = null
            _updatePropertiesPacket = null
        }
    
    private var _addOperation: AddBossBarOperation? = null
    val addOperation: AddBossBarOperation
        get() {
            if (_addOperation == null) {
                _addOperation = AddBossBarOperation(this.name, progress, color, overlay, darkenScreen, playMusic, createWorldFog)
            }
            return _addOperation!!
        }
    
    private var _updateNameOperation: UpdateNameBossBarOperation? = null
    val updateNameOperation: UpdateNameBossBarOperation
        get() {
            if (_updateNameOperation == null) {
                _updateNameOperation = UpdateNameBossBarOperation(this.name)
            }
            return _updateNameOperation!!
        }
    
    private var _updateProgressOperation: UpdateProgressBossBarOperation? = null
    val updateProgressOperation: UpdateProgressBossBarOperation
        get() {
            if (_updateProgressOperation == null) {
                _updateProgressOperation = UpdateProgressBossBarOperation(progress)
            }
            return _updateProgressOperation!!
        }
    
    private var _updateStyleOperation: UpdateStyleBossBarOperation? = null
    val updateStyleOperation: UpdateStyleBossBarOperation
        get() {
            if (_updateStyleOperation == null) {
                _updateStyleOperation = UpdateStyleBossBarOperation(color, overlay)
            }
            return _updateStyleOperation!!
        }
    
    private var _updatePropertiesOperation: UpdatePropertiesBossBarOperation? = null
    val updatePropertiesOperation: UpdatePropertiesBossBarOperation
        get() {
            if (_updatePropertiesOperation == null) {
                _updatePropertiesOperation = UpdatePropertiesBossBarOperation(darkenScreen, playMusic, createWorldFog)
            }
            return _updatePropertiesOperation!!
        }
    
    val removeOperation = RemoveBossBarOperation
    
    private var _addPacket: ClientboundBossEventPacket? = null
    val addPacket: ClientboundBossEventPacket
        get() {
            if (_addPacket == null) {
                _addPacket = ClientboundBossEventPacket(id, addOperation)
            }
            return _addPacket!!
        }
    
    private var _updateNamePacket: ClientboundBossEventPacket? = null
    val updateNamePacket: ClientboundBossEventPacket
        get() {
            if (_updateNamePacket == null) {
                _updateNamePacket = ClientboundBossEventPacket(id, updateNameOperation)
            }
            return _updateNamePacket!!
        }
    
    private var _updateProgressPacket: ClientboundBossEventPacket? = null
    val updateProgressPacket: ClientboundBossEventPacket
        get() {
            if (_updateProgressPacket == null) {
                _updateProgressPacket = ClientboundBossEventPacket(id, updateProgressOperation)
            }
            return _updateProgressPacket!!
        }
    
    private var _updateStylePacket: ClientboundBossEventPacket? = null
    val updateStylePacket: ClientboundBossEventPacket
        get() {
            if (_updateStylePacket == null) {
                _updateStylePacket = ClientboundBossEventPacket(id, updateStyleOperation)
            }
            return _updateStylePacket!!
        }
    
    private var _updatePropertiesPacket: ClientboundBossEventPacket? = null
    val updatePropertiesPacket: ClientboundBossEventPacket
        get() {
            if (_updatePropertiesPacket == null) {
                _updatePropertiesPacket = ClientboundBossEventPacket(id, updatePropertiesOperation)
            }
            return _updatePropertiesPacket!!
        }
    
    val removePacket = ClientboundBossEventPacket(id, removeOperation)
    
    companion object {
        
        fun of(id: UUID, operation: AddBossBarOperation) = BossBar(
            id,
            operation.name,
            operation.progress,
            operation.color,
            operation.overlay,
            operation.darkenScreen,
            operation.playMusic,
            operation.createWorldFog
        )
        
    }
    
}