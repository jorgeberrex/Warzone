package com.minehut.warzone.module.modules.chat;

import com.minehut.warzone.Warzone;
import com.minehut.warzone.module.Module;
import com.minehut.warzone.module.modules.team.TeamModule;
import com.minehut.warzone.user.WarzoneUser;
import com.minehut.warzone.user.event.WarzoneUserChatEvent;
import com.minehut.warzone.util.Teams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;

public class ChatModule implements Module {

    private HashMap<String, String> selectedChannels = new HashMap<>();

    @Override
    public void unload() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        if(event.isCancelled()) return;
        event.setCancelled(true);

//        String channel = "all";
//        if (selectedChannels.containsKey(event.getPlayer().getName())) {
//            channel = selectedChannels.get(event.getPlayer().getName());
//        }
//
//        channel = "all";

        Bukkit.getScheduler().scheduleSyncDelayedTask(Warzone.getInstance(), new Runnable() {
            @Override
            public void run() {
                event.getPlayer().performCommand("all " + event.getMessage());
            }
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!this.selectedChannels.containsKey(event.getPlayer().getName())) {
            this.selectedChannels.put(event.getPlayer().getName(), "t");
        }
    }

    public void sendMessage(Player player, String channel, boolean teamOnly, String message) {
        TeamModule teamModule = Teams.getTeamByPlayer(player).get();

        WarzoneUserChatEvent chatEvent = new WarzoneUserChatEvent(player, message);
        String formattedName = chatEvent.getPrefix() + player.getName();

        String s;
        if (teamOnly) {
            s = teamModule.getColor() + "[TEAM CHAT] " + formattedName
                    + ChatColor.WHITE + ": ";
        } else {
            s = teamModule.getColor() + "[" + String.valueOf(teamModule.getName().charAt(0)) + "] " + formattedName
                    + ChatColor.WHITE + ": ";
        }

        s += ChatColor.WHITE;
        s += message;

        if (teamOnly) {
            for (Player other : Bukkit.getOnlinePlayers()) {
                com.google.common.base.Optional<TeamModule> otherTeamOptional = Teams.getTeamByPlayer(other);
                if (otherTeamOptional.isPresent()) {
                    TeamModule otherTeam = otherTeamOptional.get();
                    if (otherTeam.isObserver() || otherTeam == teamModule) {
                        other.sendMessage(s);
                    }
                }
            }
        } else {
            for (Player other : Bukkit.getOnlinePlayers()) {
                other.sendMessage(s);
            }
        }

        //Allow the console to see the messages.
        System.out.println(s);
    }

    public void setChannel(Player player, String channel) {
        this.selectedChannels.put(player.getName(), channel);
    }
}
