package com.songoda.ultimatetimber.manager;

import com.songoda.ultimatetimber.UltimateTimber;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ChoppingManager extends Manager {

    private Set<UUID> enabledPlayers;
    private final Map<UUID, Boolean> cooldownedPlayers;
    private boolean useCooldown;
    private int cooldownAmount;
    private final File playersFile;

    public ChoppingManager(UltimateTimber ultimateTimber) {
        super(ultimateTimber);
        this.enabledPlayers = new HashSet<>();
        this.cooldownedPlayers = new HashMap<>();
        playersFile = new File(plugin.getDataFolder(), "players.yml");
    }

    @Override
    public void reload() {
        this.useCooldown = ConfigurationManager.Setting.PLAYER_TREE_TOPPLE_COOLDOWN.getBoolean();
        this.cooldownAmount = ConfigurationManager.Setting.PLAYER_TREE_TOPPLE_COOLDOWN_LENGTH.getInt();
    }

    @Override
    public void disable() {
        this.enabledPlayers.clear();
        this.cooldownedPlayers.clear();
    }

    public void loadPlayers() {
        if (playersFile.exists()) {
            YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playersFile);
            enabledPlayers = playerConfig.getStringList("players").stream().map(UUID::fromString).collect(Collectors.toSet());
        }
    }

    public void savePlayers() {
        try {
            playersFile.createNewFile();
            YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playersFile);
            playerConfig.set("players", enabledPlayers.stream().map(UUID::toString).collect(Collectors.toList()));
            playerConfig.save(playersFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Toggles a player's chopping status
     *
     * @param player The player to toggle
     * @return True if the player has chopping enabled, or false if they have it disabled
     */
    public boolean togglePlayer(Player player) {
        if (this.enabledPlayers.contains(player.getUniqueId())) {
            this.enabledPlayers.remove(player.getUniqueId());
            CompletableFuture.runAsync(this::savePlayers);
            return false;
        } else {
            this.enabledPlayers.add(player.getUniqueId());
            CompletableFuture.runAsync(this::savePlayers);
            return true;
        }
    }

    /**
     * Checks if a player has chopping enabled
     *
     * @param player The player to check
     * @return True if the player has chopping enabled, or false if they have it disabled
     */
    public boolean isChopping(Player player) {
        return this.enabledPlayers.contains(player.getUniqueId());
    }

    /**
     * Sets a player into cooldown
     *
     * @param player The player to cooldown
     */
    public void cooldownPlayer(Player player) {
        if (!this.useCooldown || player.hasPermission("ultimatetimber.bypasscooldown"))
            return;

        this.cooldownedPlayers.put(player.getUniqueId(), false);

        Bukkit.getScheduler().scheduleSyncDelayedTask(UltimateTimber.getInstance(), () ->
                this.cooldownedPlayers.remove(player.getUniqueId()), this.cooldownAmount * 20L);
    }

    /**
     * Checks if a player is in cooldown
     *
     * @param player The player to check
     * @return True if the player can topple trees, otherwise false
     */
    public boolean isInCooldown(Player player) {
        boolean cooldowned = this.useCooldown && this.cooldownedPlayers.containsKey(player.getUniqueId());
        if (cooldowned && !this.cooldownedPlayers.get(player.getUniqueId())) {
            this.plugin.getLocale().getMessage("event.on.cooldown").sendPrefixedMessage(player);
            this.cooldownedPlayers.replace(player.getUniqueId(), true);
        }
        return cooldowned;
    }

}
