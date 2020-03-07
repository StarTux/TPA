package com.winthier.tpa;

import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

@RequiredArgsConstructor
final class BringCommand implements CommandExecutor {
    final TPAPlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) return false;
        Player player = (sender instanceof Player) ? (Player) sender : null;
        if (player == null) {
            sender.sendMessage("Player expected");
            return true;
        }
        String playerName = args[0];
        Player target = plugin.getServer().getPlayer(playerName);
        if (target == null) {
            Util.msg(player, "&cPlayer not found: %s.", playerName);
            return true;
        }
        TPARequest request = plugin.fetchRequest(target);
        if (request == null) {
            Util.msg(player, "&c%s did not request a teleport, or it expired.", target.getName());
            return true;
        } else {
            if (target.teleport(player, PlayerTeleportEvent.TeleportCause.COMMAND)) {
                Util.msg(target, "&3&lTPA&r %s accepted your teleport request.", player.getName());
                Util.msg(player, "&3&lTPA&r Teleporting %s to you.", target.getName());
                plugin.putOnLongCooldown(target);
            } else {
                Util.msg(target, "&3&lTPA&c Teleporting to %s failed.", player.getName());
                Util.msg(player, "&3&lTPA&c Bringing %s failed.", target.getName());
            }
            return true;
        }
    }
}
