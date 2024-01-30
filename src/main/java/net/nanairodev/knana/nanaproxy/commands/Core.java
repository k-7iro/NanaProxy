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
import java.util.UUID;

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
                langs.put(lFile.getName().split("\\.(?=[^.]+$)")[0], ConfigurationProvider.getProvider(YamlConfiguration.class).load(lFile));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static UUID SearchUUID(String name) {
        UUID result = null;
        for (String key : Events.data.getKeys()) {
            if (Events.data.getString(key+".Name").equals(name)) {
                result = UUID.fromString(key);
                break;
            }
        }
        return result;
    }

    public String getLocaleMessage(String key, String server) {
        String msg;
        Configuration lang = langs.get(config.getString("Language"));
        msg = String.format(lang.getString(key), server);
        return msg;
    }

    public void execute(CommandSender sender, String[] args) {
        String msg;
        if (args.length == 0) {
            msg = getLocaleMessage("CommandMessages.CoreCommand.Info", plugin.getDescription().getVersion());
        } else if (args[0].equals("reload")) {
            try {
                Events.reload();
                Lobby.reload();
                Core.reload();
            } catch (IOException e) {
                e.printStackTrace();
            }
            msg = getLocaleMessage("CommandMessages.CoreCommand.Reloaded", "");
        } else if (args[0].equals("kick")) {
            if (args.length == 1) {
                msg = getLocaleMessage("CommandMessages.CoreCommand.MissingPlayer", "");
            } else {
                ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[1]);
                if (target == null) {
                    msg = getLocaleMessage("CommandMessages.CoreCommand.UnknownPlayer", args[1]);
                } else {
                    TextComponent kmsg;
                    if (args.length == 2) {
                        kmsg = new TextComponent(getLocaleMessage("OtherMessages.Kicked", ""));
                    } else {
                        StringBuilder reason = new StringBuilder();
                        for (int i=2; i<=(args.length-1); i++) {
                            if (i != 2) {reason.append(" ");}
                            reason.append(args[i]);
                        }
                        kmsg = new TextComponent(getLocaleMessage("OtherMessages.KickedWithReason", reason.toString()));
                    }
                    target.disconnect(kmsg);
                    msg = getLocaleMessage("CommandMessages.CoreCommand.Kicked", args[1]);
                }
            }
        } else if (args[0].equals("lkick")||args[0].equals("lobbykick")||args[0].equals("hkick")||args[0].equals("hubkick")) {
            if (args.length == 1) {
                msg = getLocaleMessage("CommandMessages.CoreCommand.MissingPlayer", "");
            } else {
                ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[1]);
                if (target == null) {
                    msg = getLocaleMessage("CommandMessages.CoreCommand.UnknownPlayer", args[1]);
                } else {
                    TextComponent kmsg;
                    if (args.length == 2) {
                        kmsg = new TextComponent(getLocaleMessage("OtherMessages.Kicked", ""));
                    } else {
                        StringBuilder reason = new StringBuilder();
                        for (int i=2; i<=(args.length-1); i++) {
                            if (i != 2) {reason.append(" ");}
                            reason.append(args[i]);
                        }
                        kmsg = new TextComponent(getLocaleMessage("OtherMessages.KickedWithReason", reason.toString()));
                    }
                    target.connect(ProxyServer.getInstance().getServerInfo(config.getString("LobbyServer.ServerID")));
                    target.sendMessage(kmsg);
                    msg = getLocaleMessage("CommandMessages.CoreCommand.LobbyKicked", args[1]);
                }
            }
        } else if (args[0].equals("ban")) {
            if (args.length == 1) {
                msg = getLocaleMessage("CommandMessages.CoreCommand.MissingPlayer", "");
            } else {
                UUID tUUID = SearchUUID(args[1]);
                if (tUUID == null) {
                    msg = getLocaleMessage("CommandMessages.CoreCommand.UnknownPlayer", args[1]);
                } else if (Events.data.getBoolean(tUUID +".Banned")) {
                    msg = getLocaleMessage("CommandMessages.CoreCommand.AlreadyBanned", args[1]);
                } else {
                    TextComponent btext;
                    ProxiedPlayer target = ProxyServer.getInstance().getPlayer(tUUID);
                    if (args.length == 2) {
                        btext = new TextComponent(getLocaleMessage("OtherMessages.Banned", ""));
                    } else {
                        StringBuilder reason = new StringBuilder();
                        for (int i=2; i<=(args.length-1); i++) {
                            if (i != 2) {reason.append(" ");}
                            reason.append(args[i]);
                        }
                        Events.data.set(tUUID +".BanReason", reason.toString());
                        btext = new TextComponent(getLocaleMessage("OtherMessages.BannedWithReason", reason.toString()));
                    }
                    if (target != null) {target.disconnect(btext);}
                    Events.data.set(tUUID +".Banned", true);
                    msg = getLocaleMessage("CommandMessages.CoreCommand.Banned", args[1]);
                }
            }
        } else if (args[0].equals("unban")) {
            if (args.length == 1) {
                msg = getLocaleMessage("CommandMessages.CoreCommand.MissingPlayer", "");
            } else {
                UUID tUUID = SearchUUID(args[1]);
                if (tUUID == null) {
                    msg = getLocaleMessage("CommandMessages.CoreCommand.UnknownPlayer", args[1]);
                } else if (!Events.data.getBoolean(tUUID +".Banned")) {
                    msg = getLocaleMessage("CommandMessages.CoreCommand.AlreadyUnbanned", args[1]);
                } else {
                    Events.data.set(tUUID +".Banned", false);
                    Events.data.set(tUUID +".BannedReason", null);
                    msg = getLocaleMessage("CommandMessages.CoreCommand.Unbanned", args[1]);
                }
            }
        } else {
            msg = getLocaleMessage("CommandMessages.CoreCommand.Unknown", "");
        }
        if ((sender instanceof ProxiedPlayer)) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            TextComponent tcmsg = new TextComponent(msg);
            player.sendMessage(tcmsg);
        } else {
            ProxyServer.getInstance().getLogger().info(msg);
        }
    }
}
