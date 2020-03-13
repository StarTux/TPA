package com.winthier.tpa;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
final class TPACommand implements CommandExecutor {
    final TPAPlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) return false;
        if (args[0].equalsIgnoreCase("-reload") && sender.hasPermission("tpa.admin")) {
            plugin.reconfigure();
            sender.sendMessage("[TPA] Configuration reloaded");
            return true;
        }
        Player player = (sender instanceof Player) ? (Player) sender : null;
        if (player == null) {
            sender.sendMessage("Player expected");
            return true;
        }
        int cooldown = plugin.getCooldownInSeconds(player);
        if (cooldown > 0) {
            Util.msg(player, "&cYou have to wait %s.", Util.formatSeconds(cooldown));
            return true;
        }
        String targetName = args[0];
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            Util.msg(player, "&cPlayer not found: %s.", targetName);
            return true;
        }
        if (target.equals(player)) {
            Util.msg(player, "&cYou cannot teleport to yourself.");
            return true;
        }
        plugin.storeRequest(player, target);
        Util.msg(player, "&3&lTPA&r request sent to %s.", target.getName());
        List<Object> msg = new ArrayList<>();
        msg.add(Util.format("&3&lTPA&r %s requests a teleport. Click to accept: ",
                            player.getName()));
        msg.add(Util.button("[&3Bring&r]",
                            "&a/bring " + player.getName()
                            + "\n&oTPA\nTeleport this player\nto you.",
                            "/bring " + player.getName()));
        Util.tellRaw(target, msg);
        target.playSound(target.getEyeLocation(), plugin.getSound(), 1.0F, 1.0F);
        plugin.putOnShortCooldown(player);
        return true;
    }
}
