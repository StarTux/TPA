package com.winthier.tpa;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandArgCompleter;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.core.event.player.PlayerTPAEvent;
import com.winthier.connect.Connect;
import com.winthier.connect.ConnectRemotePlayer;
import com.winthier.playercache.PlayerCache;
import java.util.UUID;
import org.bukkit.entity.Player;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
import static net.kyori.adventure.text.format.NamedTextColor.*;

final class BringCommand extends AbstractCommand<TPAPlugin> {
    protected BringCommand(final TPAPlugin plugin) {
        super(plugin, "bring");
    }

    @Override
    protected void onEnable() {
        rootNode.arguments("<player>")
            .description("Accept teleport request")
            .completers(CommandArgCompleter.NULL)
            .playerCaller(this::bring);
    }

    private boolean bring(Player player, String[] args) {
        if (args.length != 1) return false;
        PlayerCache target = PlayerCache.require(args[0]);
        UUID request = plugin.fetchRequest(target.uuid);
        if (request == null || !request.equals(player.getUniqueId())) {
            throw new CommandWarn(target.name + " did not request a teleport or it expired");
        }
        plugin.deleteRequest(target.uuid);
        String targetServer = Connect.getInstance().findServerOfPlayer(target.uuid);
        if (targetServer == null) {
            throw new CommandWarn("Player not found: " + target.name);
        }
        if (!new PlayerTPAEvent(target.uuid, player, true).callEvent()) {
            throw new CommandWarn("You cannot accept this TPA right now");
        }
        new ConnectRemotePlayer(target.uuid, target.name, targetServer).bring(plugin, player.getLocation(), targetPlayer -> {
                if (targetPlayer == null) {
                    player.sendMessage(text("Teleport failed: " + target.name, RED));
                    return;
                }
                targetPlayer.sendMessage(join(noSeparators(),
                                              text("TPA ", DARK_AQUA),
                                              text(player.getName() + " accepted your teleport request", WHITE)));
                player.sendMessage(join(noSeparators(),
                                        text("TPA ", DARK_AQUA),
                                        text("Teleported " + target.name + " to you", WHITE)));
                plugin.putOnCooldown(targetPlayer.getUniqueId(), 60L);
        });
        return true;
    }
}
