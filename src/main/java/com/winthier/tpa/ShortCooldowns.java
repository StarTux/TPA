package com.winthier.tpa;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class ShortCooldowns {
    final Map<UUID, Long> cooldowns = new HashMap<>();

    Long getCooldown(UUID uuid) {
        return cooldowns.get(uuid);
    }

    int getCooldownInSeconds(UUID uuid) {
        Long millis = getCooldown(uuid);
        if (millis == null) return 0;
        long cd = millis.longValue() - System.currentTimeMillis();
        if (cd < 0L) {
            return 0;
        } else {
            return (int)(cd / 1000L);
        }
    }

    void setCooldownInSeconds(UUID uuid, int seconds) {
        long cooldown = System.currentTimeMillis() + (long)seconds * 1000L;
        cooldowns.put(uuid, Long.valueOf(cooldown));
    }
}
