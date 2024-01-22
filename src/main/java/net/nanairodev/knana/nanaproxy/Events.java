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
    private static final HashMap<String, Configuration> langs = new HashMap<>();
    public Events(NanaProxy plugin) {
        Events.plugin = plugin;
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "config.yml"));
            File lFolder = new File(plugin.getDataFolder(), "lang");
            for (File lFile : Objects.requireNonNull(lFolder.listFiles())) {
                langs.put(lFile.getName().split("\\.(?=[^\\.]+$)")[0], ConfigurationProvider.getProvider(YamlConfiguration.class).load(lFile));
                ProxyServer.getInstance().getLogger().info(String.format("[NanaProxy] Loaded Lang: %s", lFile.getName().split("\\.(?=[^\\.]+$)")[0]));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reload() {
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "config.yml"));
            File lFolder = new File(plugin.getDataFolder(), "lang");
            for (File lFile : Objects.requireNonNull(lFolder.listFiles())) {
                langs.put(lFile.getName().split("\\.(?=[^\\.]+$)")[0], ConfigurationProvider.getProvider(YamlConfiguration.class).load(lFile));
                ProxyServer.getInstance().getLogger().info(String.format("[NanaProxy] Loaded Lang: %s", lFile.getName().split("\\.(?=[^\\.]+$)")[0]));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String getLocaleMessage(String key, ProxiedPlayer player, String server) {
        String msg = "";
        Configuration lang = langs.get(config.getString("Language"));
        msg = String.format(lang.getString(key), player.getName(), server);
        return msg;
    }

    public void broadcast(String msg) {
        TextComponent tc = new TextComponent(msg);
        for (ProxiedPlayer lp : ProxyServer.getInstance().getPlayers()) {
            lp.sendMessage(tc);
        }
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        if (config.getBoolean("LogMessages.Enable")) {
            ProxyServer.getInstance().getLogger().info(getLocaleMessage("LogMessages.JoinNetwork", event.getPlayer(), ""));
        }
        if (config.getBoolean("PlayerMessages.Enable")) {
            broadcast(getLocaleMessage("BroadcastMessages.JoinNetwork", event.getPlayer(), ""));
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        if (config.getBoolean("LogMessages.Enable")) {
            ProxyServer.getInstance().getLogger().info(getLocaleMessage("LogMessages.LeaveNetwork", event.getPlayer(), ""));
        }
        if (config.getBoolean("PlayerMessages.Enable")) {
            broadcast(getLocaleMessage("BroadcastMessages.LeaveNetwork", event.getPlayer(), ""));
        }
    }

    @EventHandler
    public void onSvrConnect(ServerConnectedEvent event) {
        if (config.getBoolean("LogMessages.Enable")) {
            ProxyServer.getInstance().getLogger().info(getLocaleMessage("LogMessages.MoveServer", event.getPlayer(), event.getServer().getInfo().getName()));
        }
        if (config.getBoolean("PlayerMessages.Enable")) {
            broadcast(getLocaleMessage("BroadcastMessages.MoveServer", event.getPlayer(), event.getServer().getInfo().getName()));
        }
    }

    @EventHandler
    public void onSvrKick(ServerKickEvent event) {
        if ((config.getBoolean("LobbyServer.SendOnKick") && (!event.getKickedFrom().getName().equals(config.getString("LobbyServer.ServerID"))))) {
            TextComponent msg = new TextComponent(getLocaleMessage("BroadcastMessages.MoveServer", event.getPlayer(), config.getString("LobbyServer.ServerID")));
            event.getPlayer().sendMessage(msg);
            event.setCancelServer(ProxyServer.getInstance().getServerInfo(config.getString("LobbyServer.ServerID")));
            event.setCancelled(true);
        }
    }
}
