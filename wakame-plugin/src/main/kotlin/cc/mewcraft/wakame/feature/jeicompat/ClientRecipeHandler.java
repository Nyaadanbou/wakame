package cc.mewcraft.wakame.feature.jeicompat;

import cc.mewcraft.messaging2.ServerInfoProvider;
import cc.mewcraft.wakame.KoishPlugin;
import cc.mewcraft.wakame.messaging.MessagingManager;
import cc.mewcraft.wakame.messaging.handler.JEICompatPacketHandler;
import cc.mewcraft.wakame.messaging.packet.JEICompatSyncPacket;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.world.item.crafting.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ClientRecipeHandler implements Listener {

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        if (!JetCompatConfig.INSTANCE.getEnable()) return;
        final Player bukkitPlayer = event.getPlayer();
        final ServerPlayer serverPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
        final MinecraftServer server = serverPlayer.level().getServer();
        final RecipeManager recipeManager = server.getRecipeManager();
        Bukkit.getServer().getScheduler().runTaskLater(KoishPlugin.INSTANCE, () -> {
            if (JEICompatPacketHandler.INSTANCE.has(bukkitPlayer.getUniqueId())) return;
            bukkitPlayer.sendMessage(Component.text("JEI 兼容: 同步数据中...").color(NamedTextColor.GRAY));
            RecipeMap recipeMap = recipeManager.recipes;
            RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), server.registryAccess());
            String brand = bukkitPlayer.getClientBrandName();
            if (brand == null) return; // Unknown brand, do not send any custom payload
            if (brand.equalsIgnoreCase("fabric")) {
                sendFabricPayload(serverPlayer, recipeMap, buffer);
            } else if (brand.equalsIgnoreCase("neoforge")) {
                sendNeoForgePayload(serverPlayer, server, recipeMap, buffer);
            }
        }, 20L);
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        MessagingManager.Impl.queuePacket(() -> new JEICompatSyncPacket(ServerInfoProvider.Impl.getServerId(), player.getUniqueId()));
    }

    private static void sendNeoForgePayload(ServerPlayer player, MinecraftServer server, RecipeMap recipeMap, RegistryFriendlyByteBuf buffer) {
        List<RecipeType<?>> allRecipeTypes = BuiltInRegistries.RECIPE_TYPE.stream().toList();
        NeoforgeRecipeSyncPayload payload = NeoforgeRecipeSyncPayload.create(allRecipeTypes, recipeMap);
        NeoforgeRecipeSyncPayload.STREAM_CODEC.encode(buffer, payload);
        byte[] bytes = new byte[buffer.writerIndex()];
        buffer.getBytes(0, bytes);
        sendPayload(player, Identifier.fromNamespaceAndPath("neoforge", "recipe_content"), bytes);
        player.connection.send(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(server.registries())));
    }

    private static void sendFabricPayload(ServerPlayer player, RecipeMap recipeMap, RegistryFriendlyByteBuf buffer) {
        var list = new ArrayList<FabricRecipeSyncPayload.Entry>();
        var seen = new HashSet<RecipeSerializer<?>>();
        for (RecipeSerializer<?> serializer : BuiltInRegistries.RECIPE_SERIALIZER) {
            if (!seen.add(serializer)) continue; // skip duplicates
            List<RecipeHolder<?>> recipes = new ArrayList<>();
            for (RecipeHolder<?> holder : recipeMap.values()) {
                if (holder.value().getSerializer() == serializer && holder.id().identifier().getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
                    recipes.add(holder);
                }
            }
            if (!recipes.isEmpty()) {
                RecipeSerializer<?> entrySerializer = recipes.getFirst().value().getSerializer();
                list.add(new FabricRecipeSyncPayload.Entry(entrySerializer, recipes));
            }
        }
        var payload = new FabricRecipeSyncPayload(list);
        FabricRecipeSyncPayload.CODEC.encode(buffer, payload);
        byte[] bytes = new byte[buffer.writerIndex()];
        buffer.getBytes(0, bytes);
        sendPayload(player, Identifier.fromNamespaceAndPath("fabric", "recipe_sync"), bytes);
    }

    private static void sendPayload(ServerPlayer player, Identifier id, byte[] bytes) {
        player.connection.send(new ClientboundCustomPayloadPacket(new DiscardedPayload(id, bytes)));
    }
}