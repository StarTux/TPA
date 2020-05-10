package com.winthier.tpa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class TPAPlugin extends JavaPlugin {
    private final Map<UUID, TPARequest> tpaRequests = new HashMap<>();
    private LongCooldowns longCooldowns;
    private ShortCooldowns shortCooldowns;
    private int longCooldown;
    private int shortCooldown;
    private int expiry;
    private Sound sound;
    Set<String> disabledWorlds = new HashSet<>();

    public TPAPlugin() {
        longCooldowns = null;
        shortCooldowns = null;
        longCooldown = 60;
        shortCooldown = 10;
        expiry = 120;
        sound = Sound.ENTITY_ARROW_HIT_PLAYER;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reconfigure();
        getCommand("tpa").setExecutor(new TPACommand(this));
        getCommand("bring").setExecutor(new BringCommand(this));
    }

    @Override
    public void onDisable() {
        tpaRequests.clear();
    }

    LongCooldowns getLongCooldowns() {
        if (longCooldowns == null) longCooldowns = new LongCooldowns(this);
        return longCooldowns;
    }

    ShortCooldowns getShortCooldowns() {
        if (shortCooldowns == null) shortCooldowns = new ShortCooldowns();
        return shortCooldowns;
    }

    void reconfigure() {
        reloadConfig();
        shortCooldowns = null;
        longCooldowns = null;
        shortCooldown = getConfig().getInt("ShortCooldown", shortCooldown);
        longCooldown = getConfig().getInt("LongCooldown", longCooldown);
        expiry = getConfig().getInt("Expiry", expiry);
        try {
            sound = Sound.valueOf(getConfig().getString("Sound"));
        } catch (IllegalArgumentException illegalargumentexception) { }
        disabledWorlds.clear();
        disabledWorlds.addAll(getConfig().getStringList("DisabledWorlds"));
    }

    int getCooldownInSeconds(Player player) {
        java.util.UUID uuid = player.getUniqueId();
        return Math.max(getShortCooldowns().getCooldownInSeconds(uuid),
                        getLongCooldowns().getCooldownInSeconds(uuid));
    }

    void storeRequest(Player sender, Player target) {
        long exp = System.currentTimeMillis() + (long) expiry * 1000L;
        TPARequest request = new TPARequest(sender.getUniqueId(), target.getUniqueId(), exp);
        tpaRequests.put(sender.getUniqueId(), request);
    }

    TPARequest fetchRequest(Player player) {
        TPARequest result = (TPARequest) tpaRequests.remove(player.getUniqueId());
        if (result == null) return null;
        if (result.getExpiry() < System.currentTimeMillis()) return null;
        return result;
    }

    void putOnShortCooldown(Player player) {
        getShortCooldowns().setCooldownInSeconds(player.getUniqueId(), shortCooldown);
    }

    void putOnLongCooldown(Player player) {
        getLongCooldowns().setCooldownInSeconds(player.getUniqueId(), longCooldown);
        getLongCooldowns().save();
    }
}
