# NanaProxy
Why doesn't Bungeecord broadcast to the network when someone joins the network? Why doesn't Bungeecord transfer players kicked from a server to other servers? This plugin is a good way to solve them.

# Overview
This plugin has four main features.
- Broadcast to the entire network when players join, leave, or move servers.
- Sends to the console when a player joins the network, leaves the network, or moves servers.
- Adds the /lobby and /hub commands (There is no difference between the two commands) that can be used when the /server command cannot be used for some reason, and even if it can be used, can reduce the number of input characters.
- If a player is kicked on a server other than the lobby due to shutdown, etc., the kicked player will be transferred to the lobby server.
Most features can be disabled in the config. You can also change the message to other languages.

# Commands
- **/nanaproxy** Show plugin info.
  - **/nanaproxy reload** Reload plugin.
- **/nproxy** Alias of /nanaproxy.
- **/np** Alias of /nanaproxy.
- **/lobby** Connect to lobby server.
- **/hub** Alias of /lobby.
/lobby and /hub is available with default permissions. Other commands require nanaproxy.admin permission.

# Installation
For most Bungeecords, simply placing the downloaded plugin file into Bungeecord's plugins folder will work. However, additional work is required if:
- **If server named "lobby" does not exist.** Open the settings and change the "ServerID" of "LobbyServer" to your existing server ID. Or change the server ID of either server to "lobby".
- **If you use a language other than English.** Open the config and set "Language" to the language you want to use. If it doesn't exist, copy one of the language files in "plugins/NanaProxy/langs", rename it, and translate it.
Have fun!
