package com.winthier.tpa;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandArgCompleter;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.core.command.RemotePlayer;
import com.cavetale.core.event.player.PlayerTPAEvent;
import com.cavetale.core.font.DefaultFont;
import com.winthier.chat.ChatPlugin;
import com.winthier.connect.Connect;
import com.winthier.connect.ConnectPlugin;
import com.winthier.connect.payload.OnlinePlayer;
import com.winthier.playercache.PlayerCache;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import static com.cavetale.core.font.Unicode.tiny;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.*;

final class TPACommand extends AbstractCommand<TPAPlugin> {
    protected TPACommand(final TPAPlugin plugin) {
        super(plugin, "tpa");
    }

    @Override
    protected void onEnable() {
        rootNode.arguments("<player>")
            .description("Request teleport")
            .completers(CommandArgCompleter.supplyIgnoreCaseList(() -> {
                        ArrayList<String> result = new ArrayList<>();
                        for (OnlinePlayer it : Connect.getInstance().getOnlinePlayers()) {
                            result.add(it.getName());
                        }
                        return result;
                    }))
            .remotePlayerCaller(this::tpa);
    }

    private boolean tpa(RemotePlayer player, String[] args) {
        if (args.length != 1) return false;
        if (!player.hasPermission("tpa.nocooldown")) {
            long cooldown = plugin.getCooldown(player.getUniqueId());
            if (cooldown > 0) {
                throw new CommandWarn("Please wait " + cooldown + "s");
            }
        }
        PlayerCache target = PlayerCache.require(args[0]);
        if (target.uuid.equals(player.getUniqueId())) {
            throw new CommandWarn("You cannot teleport to yourself");
        }
        if (ChatPlugin.getInstance().doesIgnore(target.uuid, player.getUniqueId())) {
            throw new CommandWarn("Player not found: " + target.name);
        }
        Player targetPlayer = Bukkit.getPlayer(target.uuid);
        if (targetPlayer == null) {
            if (!player.isPlayer()) {
                throw new CommandWarn("Player not found: " + target.name);
            }
            String targetServer = Connect.getInstance().findServerOfPlayer(target.uuid);
            if (targetServer == null) {
                throw new CommandWarn("Player not found: " + target.name);
            }
            ConnectPlugin.getInstance().getCoreConnect().dispatchRemoteCommand(player.getPlayer(), "tpa " + target.name, targetServer);
            return true;
        }
        if (!new PlayerTPAEvent(player.getUniqueId(), targetPlayer, false).callEvent()) {
            throw new CommandWarn("You cannot TPA to " + target.name + " right now");
        }
        plugin.storeRequest(player.getUniqueId(), target.uuid);
        Connect.getInstance().broadcast("tpa.request", player.getUniqueId().toString());
        plugin.putOnCooldown(player.getUniqueId(), 30L);
        player.sendMessage(join(noSeparators(),
                                text(tiny("tpa "), DARK_AQUA),
                                text("Request sent to " + target.name, WHITE)));
        String cmd = "/bring " + player.getName();
        targetPlayer.sendMessage(join(noSeparators(),
                                      text(tiny("tpa "), DARK_AQUA),
                                      (DefaultFont.ACCEPT_BUTTON.forPlayer(targetPlayer)
                                       .hoverEvent(showText(text(cmd, GREEN)))
                                       .clickEvent(runCommand(cmd))),
                                      text(" " + player.getName(), GRAY),
                                      text(" requested a teleport", WHITE)));
        targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, SoundCategory.MASTER, 1.0f, 1.0f);
        return true;
    }
}
