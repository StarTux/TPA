package com.winthier.tpa;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;

final class LongCooldowns {
    final TPAPlugin plugin;
    private YamlConfiguration config;
    static final String FILENAME = "cooldowns.yml";

    File saveFile() {
        return new File(plugin.getDataFolder(), "cooldowns.yml");
    }

    LongCooldowns(TPAPlugin plugin) {
        config = null;
        this.plugin = plugin;
        config = YamlConfiguration.loadConfiguration(saveFile());
        long currentTime = System.currentTimeMillis();
        Iterator iterator = config.getKeys(false).iterator();
        do {
            if (!iterator.hasNext()) break;
            String userKey = (String)iterator.next();
            long time = config.getLong(userKey);
            if (time < currentTime) config.set(userKey, null);
        } while (true);
    }

    void save() {
        try {
            config.save(saveFile());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    Long getCooldown(UUID uuid) {
        return Long.valueOf(config.getLong(uuid.toString()));
    }

    int getCooldownInSeconds(UUID uuid) {
        Long millis = getCooldown(uuid);
        if (millis == null) return 0;
        long cd = millis.longValue() - System.currentTimeMillis();
        if (cd < 0L) return 0;
        return (int)(cd / 1000L);
    }

    void setCooldownInSeconds(UUID uuid, int seconds) {
        long cooldown = System.currentTimeMillis() + (long)seconds * 1000L;
        config.set(uuid.toString(), Long.valueOf(cooldown));
    }
}
