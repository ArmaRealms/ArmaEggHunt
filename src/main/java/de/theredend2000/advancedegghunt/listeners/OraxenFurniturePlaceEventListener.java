package de.theredend2000.advancedegghunt.listeners;

import de.theredend2000.advancedegghunt.Main;
import de.theredend2000.advancedegghunt.managers.PermissionManager;
import de.theredend2000.advancedegghunt.managers.SoundManager;
import de.theredend2000.advancedegghunt.managers.eggmanager.EggManager;
import de.theredend2000.advancedegghunt.util.enums.Permission;
import de.theredend2000.advancedegghunt.util.messages.MessageKey;
import de.theredend2000.advancedegghunt.util.messages.MessageManager;
import io.th0rgal.oraxen.api.events.furniture.OraxenFurniturePlaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class OraxenFurniturePlaceEventListener implements Listener {
    private final PermissionManager permissionManager = Main.getInstance().getPermissionManager();
    private final EggManager eggManager = Main.getInstance().getEggManager();
    private final SoundManager soundManager = Main.getInstance().getSoundManager();
    private final MessageManager messageManager = Main.getInstance().getMessageManager();
    private final List<UUID> placeEggsPlayers = Main.getInstance().getPlaceEggsPlayers();


    public OraxenFurniturePlaceEventListener() {
        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler(ignoreCancelled = true)
    public void onOraxenFurniturePlace(@NotNull OraxenFurniturePlaceEvent event) {
        Player player = event.getPlayer();
        if (!placeEggsPlayers.contains(player.getUniqueId())) return;

        if (permissionManager.checkPermission(player, Permission.PlaceEgg)) {
            String collection = eggManager.getEggCollectionFromPlayerData(player.getUniqueId());
            eggManager.saveEgg(player, event.getBlock().getLocation(), collection);
            player.playSound(player.getLocation(), soundManager.playEggPlaceSound(), soundManager.getSoundVolume(), 1);
        } else {
            player.sendMessage(messageManager.getMessage(MessageKey.PERMISSION_ERROR).replaceAll("%PERMISSION%", Permission.PlaceEgg.toString()));
        }
    }

}
