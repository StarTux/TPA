package com.winthier.tpa;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

final class Util {
    static Gson gson = new Gson();

    private Util() { }

    public static Object button(String chat, String tooltip, String command) {
        Map<String, Object> map = new HashMap<>();
        map.put("text", format(chat));
        Map<String, Object> map2 = new HashMap<>();
        map.put("clickEvent", map2);
        map2.put("action", "run_command");
        map2.put("value", command);
        map2 = new HashMap<>();
        map.put("hoverEvent", map2);
        map2.put("action", "show_text");
        map2.put("value", format(tooltip));
        return map;
    }

    public static String format(String msg, Object... args) {
        msg = ChatColor.translateAlternateColorCodes('&', msg);
        if (args.length > 0) msg = String.format(msg, args);
        return msg;
    }

    public static void msg(CommandSender target, String msg, Object... args) {
        target.sendMessage(format(msg, args));
    }

    public static String formatSeconds(int seconds) {
        if (seconds == 1) return "one second";
        int minutes = seconds / 60;
        if (minutes == 0) {
            return new StringBuilder().append("")
                .append(seconds).append(" seconds").toString();
        }
        if (minutes == 1) {
            return "one minute";
        }
        int hours = minutes / 60;
        if (hours == 0) {
            return new StringBuilder().append("")
                .append(minutes).append(" minutes").toString();
        }
        if (hours == 1) return "one hour";
        return new StringBuilder().append("")
            .append(hours).append(" hours").toString();
    }

    public static void tellRaw(Player player, Object json) {
        String js;
        try {
            js = gson.toJson(json);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), (new StringBuilder()).append("minecraft:tellraw ").append(player.getName()).append(" ").append(js).toString());
    }
}
