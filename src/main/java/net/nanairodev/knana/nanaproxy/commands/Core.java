package net.nanairodev.knana.nanaproxy.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.nanairodev.knana.nanaproxy.Events;
import net.nanairodev.knana.nanaproxy.NanaProxy;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class Core extends Command {
    private static Configuration config = null;
    private static NanaProxy plugin = null;
    private static final HashMap<String, Configuration> langs = new HashMap<>();
    public Core(net.nanairodev.knana.nanaproxy.NanaProxy plugin) {
        super("nanaproxy", "nanaproxy.admin", "nproxy", "np");
        Core.plugin = plugin;
        try {
            reload();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reload() throws IOException {
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
            TextComponent msg;
            if (args.length == 0) {
                msg = new TextComponent(getLocaleMessage("CommandMessages.Info", plugin.getDescription().getVersion()));
            } else if (args[0].equals("reload")) {
                try {
                    Events.reload();
                    Lobby.reload();
                    Core.reload();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                msg = new TextComponent(getLocaleMessage("CommandMessages.Reloaded", ""));
            } else {
                msg = new TextComponent(getLocaleMessage("CommandMessages.Unknown", ""));
            }
            player.sendMessage(msg);
        } else {
            String msg;
            if (args.length == 0) {
                msg = getLocaleMessage("CommandMessages.Info", plugin.getDescription().getVersion());
            } else if (args[0].equals("reload")) {
                try {
                    Events.reload();
                    Lobby.reload();
                    Core.reload();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                msg = getLocaleMessage("CommandMessages.Reloaded", "");
            } else {
                msg = getLocaleMessage("CommandMessages.Unknown", "");
            }
            ProxyServer.getInstance().getLogger().info(msg);
        }
    }
}
