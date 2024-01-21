package net.nanairodev.knana.nanaproxy;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class Lobby extends Command {
    private final Configuration config;
    private final HashMap<String, Configuration> langs = new HashMap<>();
    public Lobby(NanaProxy plugin) {
        super("lobby", null, "hub");
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "config.yml"));
            File lFolder = new File(plugin.getDataFolder(), "lang");
            for (File lFile : Objects.requireNonNull(lFolder.listFiles())) {
                langs.put(lFile.getName().split("\\.(?=[^\\.]+$)")[0], ConfigurationProvider.getProvider(YamlConfiguration.class).load(lFile));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getLocaleMessage(String key, String server) {
        String msg = "";
        Configuration lang = langs.get(config.getString("Language"));
        msg = String.format(lang.getString(key), server);
        return msg;
    }

    public void execute(CommandSender sender, String[] args) {
        if ((sender instanceof ProxiedPlayer)) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            if (config.getBoolean("LobbyServer.Command")) {
                if (player.getServer().getInfo().getName().equals(config.getString("LobbyServer.ServerID"))) {
                    TextComponent msg = new TextComponent(getLocaleMessage("CommandMessages.Failed", config.getString("LobbyServer.ServerID")));
                    player.sendMessage(msg);
                } else {
                    TextComponent msg = new TextComponent(getLocaleMessage("CommandMessages.Move", config.getString("LobbyServer.ServerID")));
                    player.sendMessage(msg);
                    player.connect(ProxyServer.getInstance().getServerInfo(config.getString("LobbyServer.ServerID")));
                }
            } else {
                TextComponent msg = new TextComponent(getLocaleMessage("CommandMessages.Disabled", ""));
                player.sendMessage(msg);
            }
        } else {
            ProxyServer.getInstance().getLogger().info(getLocaleMessage("CommandMessages.Console", ""));
        }
    }
}