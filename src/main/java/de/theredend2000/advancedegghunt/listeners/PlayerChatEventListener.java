package de.theredend2000.advancedegghunt.listeners;

import de.theredend2000.advancedegghunt.Main;
import de.theredend2000.advancedegghunt.managers.inventorymanager.eggrewards.global.GlobalEggRewardsMenu;
import de.theredend2000.advancedegghunt.managers.inventorymanager.eggrewards.individual.IndividualEggRewardsMenu;
import de.theredend2000.advancedegghunt.util.messages.MessageKey;
import de.theredend2000.advancedegghunt.util.messages.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.UUID;

public class PlayerChatEventListener implements Listener {

    private final MessageManager messageManager;

    public PlayerChatEventListener() {
        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
        runTimeForPlayers();
        messageManager = Main.getInstance().getMessageManager();
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!Main.getInstance().getPlayerAddCommand().containsKey(player.getUniqueId())) {
            return;
        }
        event.setCancelled(true);

        FileConfiguration playerConfig = Main.getInstance().getPlayerEggDataManager().getPlayerData(player.getUniqueId());
        if (playerConfig.contains("Change.")) {
            String id = playerConfig.getString("Change.id");
            String collection = playerConfig.getString("Change.collection");
            FileConfiguration placedEggs = Main.getInstance().getEggDataManager().getPlacedEggs(collection);

            if (event.getMessage().equalsIgnoreCase("cancel")) {
                Main.getInstance().getPlayerAddCommand().remove(player.getUniqueId());
                player.sendMessage(messageManager.getMessage(MessageKey.COMMAND_CANCEL));
                new IndividualEggRewardsMenu(Main.getPlayerMenuUtility(player)).open(id, collection);
                return;
            }

            if (Main.getInstance().getPluginConfig().isCommandBlacklisted(event.getMessage())) {
                player.sendMessage(messageManager.getMessage(MessageKey.COMMAND_BLACKLISTED));
                return;
            }
            addCommand(placedEggs, id, event.getMessage(), collection, player, "PlacedEggs." + id + ".Rewards.");
            Main.getInstance().getPlayerAddCommand().remove(player.getUniqueId());
            playerConfig.set("Change", null);
            Main.getInstance().getPlayerEggDataManager().savePlayerData(player.getUniqueId(), playerConfig);
            new IndividualEggRewardsMenu(Main.getPlayerMenuUtility(player)).open(id, collection);
            return;
        }
        if (playerConfig.contains("GlobalChange.")) {
            String id = playerConfig.getString("GlobalChange.id");
            String collection = playerConfig.getString("GlobalChange.collection");
            FileConfiguration placedEggs = Main.getInstance().getEggDataManager().getPlacedEggs(collection);

            if (event.getMessage().equalsIgnoreCase("cancel")) {
                Main.getInstance().getPlayerAddCommand().remove(player.getUniqueId());
                player.sendMessage(messageManager.getMessage(MessageKey.COMMAND_CANCEL));
                new GlobalEggRewardsMenu(Main.getPlayerMenuUtility(player)).open(id, collection);
                return;
            }

            if (Main.getInstance().getPluginConfig().isCommandBlacklisted(event.getMessage())) {
                player.sendMessage(messageManager.getMessage(MessageKey.COMMAND_BLACKLISTED));
                return;
            }
            addCommand(placedEggs, id, event.getMessage(), collection, player, "GlobalRewards.");
            Main.getInstance().getPlayerAddCommand().remove(player.getUniqueId());
            playerConfig.set("GlobalChange", null);
            Main.getInstance().getPlayerEggDataManager().savePlayerData(player.getUniqueId(), playerConfig);
            new GlobalEggRewardsMenu(Main.getPlayerMenuUtility(player)).open(id, collection);
        }
    }

    public void addCommand(FileConfiguration placedEggs, String id, String command, String collection, Player player, String path) {
        if (placedEggs.contains(path)) {
            ConfigurationSection rewardsSection = placedEggs.getConfigurationSection(path);
            int nextNumber = 0;
            if (rewardsSection == null) {
                rewardsSection = placedEggs.createSection(path);
            }
            Set<String> keys = rewardsSection.getKeys(false);
            if (!keys.isEmpty()) {
                for (int i = 0; i <= keys.size(); i++) {
                    String key = Integer.toString(i);
                    if (!keys.contains(key)) {
                        nextNumber = i;
                        break;
                    }
                }
            }
            setConfiguration(String.valueOf(nextNumber), command, collection, path);
            player.sendMessage(messageManager.getMessage(MessageKey.COMMAND_ADD).replace("%ID%", String.valueOf(nextNumber)));
        } else {
            setConfiguration("0", command, collection, path);
            player.sendMessage(messageManager.getMessage(MessageKey.COMMAND_ADD).replace("%ID%", "0"));
        }
    }

    private void setConfiguration(String commandID, String command, String collection, String path) {
        FileConfiguration placedEggs = Main.getInstance().getEggDataManager().getPlacedEggs(collection);
        placedEggs.set(path + commandID + ".command", command);
        placedEggs.set(path + commandID + ".enabled", true);
        placedEggs.set(path + commandID + ".foundAll", false);
        Main.getInstance().getEggDataManager().savePlacedEggs(collection, placedEggs);
    }

    private void runTimeForPlayers() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : Main.getInstance().getPlayerAddCommand().keySet()) {
                    FileConfiguration playerConfig = Main.getInstance().getPlayerEggDataManager().getPlayerData(uuid);
                    int currenttime = Main.getInstance().getPlayerAddCommand().get(uuid);
                    Main.getInstance().getPlayerAddCommand().remove(uuid);
                    if (currenttime == 0) {
                        if (uuid != null) {
                            Player player = Bukkit.getPlayer(uuid);
                            player.sendMessage(messageManager.getMessage(MessageKey.COMMAND_EXPIRED));
                            playerConfig.set("Change", null);
                            playerConfig.set("GlobalChange", null);
                            Main.getInstance().getPlayerEggDataManager().savePlayerData(uuid, playerConfig);
                        }
                        return;
                    }
                    Main.getInstance().getPlayerAddCommand().put(uuid, currenttime - 1);
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 20);
    }
}
