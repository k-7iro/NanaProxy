package net.nanairodev.knana.nanaproxy;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class Events implements Listener {
    private static NanaProxy plugin = null;
    private static Configuration config = null;
    public static Configuration data = null;
    private static final HashMap<String, Configuration> langs = new HashMap<>();
    public Events(NanaProxy plugin) {
        Events.plugin = plugin;
        try {
            reload();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void reload() throws IOException {
        config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "config.yml"));
        data = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "data.yml"));
        File lFolder = new File(plugin.getDataFolder(), "lang");
        for (File lFile : Objects.requireNonNull(lFolder.listFiles())) {
            langs.put(lFile.getName().split("\\.(?=[^\\.]+$)")[0], ConfigurationProvider.getProvider(YamlConfiguration.class).load(lFile));
            ProxyServer.getInstance().getLogger().info(String.format("[NanaProxy] Loaded Lang: %s", lFile.getName().split("\\.(?=[^\\.]+$)")[0]));
        }
    }
    public String getLocaleMessageWithPlayer(String key, ProxiedPlayer player, String server) {
        String msg = "";
        Configuration lang = langs.get(config.getString("Language"));
        msg = String.format(lang.getString(key), player.getName(), server);
        return msg;
    }

    public String getLocaleMessage(String key, String server) {
        String msg = "";
        Configuration lang = langs.get(config.getString("Language"));
        msg = String.format(lang.getString(key), server);
        return msg;
    }

    public void broadcast(String msg) {
        TextComponent tc = new TextComponent(msg);
        for (ProxiedPlayer lp : ProxyServer.getInstance().getPlayers()) {
            lp.sendMessage(tc);
        }
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        if (data.contains(event.getConnection().getUniqueId().toString())) {
            if (data.getBoolean(event.getConnection().getUniqueId().toString() + ".Banned")) {
                TextComponent reason;
                if (data.contains(event.getConnection().getUniqueId().toString() + ".BanReason")) {
                    reason = new TextComponent(getLocaleMessage("OtherMessages.BannedWithReason", data.getString(event.getConnection().getUniqueId().toString() + ".BanReason")));
                } else {
                    reason = new TextComponent(getLocaleMessage("OtherMessages.Banned", ""));
                }
                event.setCancelReason(reason);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        if (data.contains(event.getPlayer().getUniqueId().toString())) {
            String key;
            String oldName = "";
            if (data.getString(event.getPlayer().getUniqueId().toString()+".Name").equals(event.getPlayer().getName())) {
                key = "LogMessages.JoinNetwork";
            } else {
                key = "LogMessages.NameChanged";
                oldName = data.getString(event.getPlayer().getUniqueId().toString()+".Name");
            }
            if (config.getBoolean("LogMessages.Enable")) {
                ProxyServer.getInstance().getLogger().info(getLocaleMessageWithPlayer(key, event.getPlayer(), oldName));
            }
            if (config.getBoolean("PlayerMessages.Enable")) {
                broadcast(getLocaleMessageWithPlayer(key, event.getPlayer(), oldName));
            }
        } else {
            data.set(event.getPlayer().getUniqueId().toString()+".Banned", false);
            data.set(event.getPlayer().getUniqueId().toString()+".Name", event.getPlayer().getName());
            if (config.getBoolean("LogMessages.Enable")) {
                ProxyServer.getInstance().getLogger().info(getLocaleMessageWithPlayer("LogMessages.JoinNetwork", event.getPlayer(), ""));
                ProxyServer.getInstance().getLogger().info(getLocaleMessage("LogMessages.FirstJoin", ""));
            }
            if (config.getBoolean("PlayerMessages.Enable")) {
                broadcast(getLocaleMessageWithPlayer("BroadcastMessages.JoinNetwork", event.getPlayer(), ""));
                broadcast(getLocaleMessage("BroadcastMessages.FirstJoin", ""));
            }
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        if (config.getBoolean("LogMessages.Enable")) {
            ProxyServer.getInstance().getLogger().info(getLocaleMessageWithPlayer("LogMessages.LeaveNetwork", event.getPlayer(), ""));
        }
        if (config.getBoolean("PlayerMessages.Enable")) {
            broadcast(getLocaleMessageWithPlayer("BroadcastMessages.LeaveNetwork", event.getPlayer(), ""));
        }
    }

    @EventHandler
    public void onSvrConnect(ServerConnectedEvent event) {
        if (config.getBoolean("LogMessages.Enable")) {
            ProxyServer.getInstance().getLogger().info(getLocaleMessageWithPlayer("LogMessages.MoveServer", event.getPlayer(), event.getServer().getInfo().getName()));
        }
        if (config.getBoolean("PlayerMessages.Enable")) {
            broadcast(getLocaleMessageWithPlayer("BroadcastMessages.MoveServer", event.getPlayer(), event.getServer().getInfo().getName()));
        }
    }

    @EventHandler
    public void onSvrKick(ServerKickEvent event) {
        if ((config.getBoolean("LobbyServer.SendOnKick") && (!event.getKickedFrom().getName().equals(config.getString("LobbyServer.ServerID"))))) {
            TextComponent msg = new TextComponent(getLocaleMessageWithPlayer("BroadcastMessages.MoveServer", event.getPlayer(), config.getString("LobbyServer.ServerID")));
            event.getPlayer().sendMessage(msg);
            event.setCancelServer(ProxyServer.getInstance().getServerInfo(config.getString("LobbyServer.ServerID")));
            event.setCancelled(true);
        }
    }
}
