package com.winthier.tpa;

import com.winthier.connect.Redis;
import java.util.UUID;
import org.bukkit.plugin.java.JavaPlugin;

public final class TPAPlugin extends JavaPlugin {
    private static final String REDIS_REQUEST_PREFIX = "tpa.request.";
    private static final String REDIS_COOLDOWN_PREFIX = "tpa.cooldown.";

    @Override
    public void onEnable() {
        new TPACommand(this).enable();
        new BringCommand(this).enable();
    }

    protected void storeRequest(UUID sender, UUID target) {
        Redis.set(REDIS_REQUEST_PREFIX + sender, target.toString(), 120L);
    }

    protected UUID fetchRequest(UUID sender) {
        String result = Redis.get(REDIS_REQUEST_PREFIX + sender);
        if (result == null) return null;
        return UUID.fromString(result);
    }

    protected void deleteRequest(UUID sender) {
        Redis.del(REDIS_REQUEST_PREFIX + sender);
    }

    protected void putOnCooldown(UUID uuid, long seconds) {
        long then = System.currentTimeMillis() + seconds * 1000L;
        Redis.set(REDIS_COOLDOWN_PREFIX + uuid, "" + then, seconds);
    }

    protected long getCooldown(UUID uuid) {
        String result = Redis.get(REDIS_COOLDOWN_PREFIX + uuid);
        if (result == null) return 0L;
        long timespan = Long.parseLong(result) - System.currentTimeMillis();
        long seconds = (timespan - 1) / 1000L + 1L;
        return Math.max(1L, seconds);
    }
}
