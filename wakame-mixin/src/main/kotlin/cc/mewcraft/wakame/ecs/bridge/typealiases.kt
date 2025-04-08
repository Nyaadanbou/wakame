package cc.mewcraft.wakame.ecs.bridge

// ECS 中的一些类名跟 Bukkit 的完全一样, 故设置一些 typealiases

typealias EWorld = com.github.quillraven.fleks.World
typealias EEntity = com.github.quillraven.fleks.Entity
typealias EComponent<T> = com.github.quillraven.fleks.Component<T>
typealias EComponentType<T> = com.github.quillraven.fleks.ComponentType<T>
@Deprecated("Use EEntity instead", ReplaceWith("EEntity", "cc.mewcraft.wakame.ecs.bridge.EEntity"))
typealias FleksEntity = com.github.quillraven.fleks.Entity
typealias BukkitEntity = org.bukkit.entity.Entity
typealias BukkitBlock = org.bukkit.block.Block
typealias BukkitPlayer = org.bukkit.entity.Player
typealias BukkitWorld = org.bukkit.World
typealias BukkitComponent = net.kyori.adventure.text.Component