package net.nanairodev.knana.nanaproxy;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.nanairodev.knana.nanaproxy.commands.Lobby;
import net.nanairodev.knana.nanaproxy.commands.Core;
import net.nanairodev.knana.nanaproxy.librarys.Metrics;

import java.io.*;
import java.nio.file.Files;

public final class NanaProxy extends Plugin {
    public Configuration config = null;

    public void makeConfig(String fName, Boolean empty) throws IOException {
        File file = new File(getDataFolder(), fName);
        if (!file.exists()) {
            if (empty) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try (InputStream in = getResourceAsStream(fName)) {
                    Files.copy(in, file.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onEnable() {
        ProxyServer.getInstance().getLogger().info("Loading NanaProxy...");
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdir();
            }
            File lFolder = new File(getDataFolder(), "lang");
            if (!lFolder.exists()) {
                lFolder.mkdir();
            }
            makeConfig("config.yml", false);
            makeConfig("lang/ja_jp.yml", false);
            makeConfig("lang/en_us.yml", false);
            makeConfig("lang/zh_cn.yml", false);
            makeConfig("data.yml", true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ProxyServer.getInstance().getPluginManager().registerListener(this, new Events(this));
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Core(this));
        if (config.getBoolean("LobbyServer.Command")) {
            ProxyServer.getInstance().getPluginManager().registerCommand(this, new Lobby(this));
        }
        Metrics metrics = new Metrics(this, 20738);
        metrics.addCustomChart(new Metrics.SimplePie("used_language", () -> config.getString("Language", "unknown")));
        ProxyServer.getInstance().getLogger().info("[NanaProxy] Successful Loaded NanaProxy!");
    }

    @Override
    public void onDisable() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(Events.data, new File(getDataFolder(), "data.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            ProxyServer.getInstance().getLogger().info("[NanaProxy] Successful Unloaded NanaProxy!");
        }
    }
}
