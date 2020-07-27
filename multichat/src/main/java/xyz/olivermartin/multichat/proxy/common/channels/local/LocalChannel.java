package xyz.olivermartin.multichat.proxy.common.channels.local;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import xyz.olivermartin.multichat.bungee.ChatControl;
import xyz.olivermartin.multichat.bungee.MultiChat;
import xyz.olivermartin.multichat.common.MultiChatUtil;
import xyz.olivermartin.multichat.proxy.common.ProxyLocalCommunicationManager;
import xyz.olivermartin.multichat.proxy.common.channels.ChannelManager;

public class LocalChannel {

	private String desc;
	private String format;
	private List<String> aliases;

	private ChannelManager manager;

	public LocalChannel(String desc, String format, List<String> aliases, ChannelManager manager) {

		this.desc = desc;
		this.format = format;
		this.aliases = aliases;

		this.manager = manager;

	}

	public String getDescription() {
		return this.desc;
	}

	public String getFormat() {
		return this.format;
	}

	public List<String> getAliases() {
		return this.aliases;
	}

	public ChannelManager getManager() {
		return this.manager;
	}

	public void distributeMessage(ProxiedPlayer sender, String message, String format, Set<UUID> otherRecipients) {

		// If the sender can't speak, or is between servers, then return
		if (sender.getServer() == null) return;

		String joined = format + message;

		for (ProxiedPlayer receiver : ProxyServer.getInstance().getPlayers()) {

			// Skip sending to this player if they shouldn't receive the message
			if (receiver.getServer() == null // Receiver is between servers
					|| manager.isHidden(receiver.getUniqueId(), "local")) // Receiver has hidden this channel
				continue;

			// If receiver is NOT in the other recipients list then leave processing (as this is local only)
			if (!otherRecipients.contains(receiver.getUniqueId())) continue;

			// If receiver ignores sender
			if (ChatControl.ignores(sender.getUniqueId(), receiver.getUniqueId(), "global_chat")) {
				ChatControl.sendIgnoreNotifications(receiver, sender, "global_chat");
				continue;
			}

			if (MultiChat.legacyServers.contains(receiver.getServer().getInfo().getName())) {
				receiver.sendMessage(TextComponent.fromLegacyText(MultiChatUtil.approximateHexCodes(joined)));
			} else {
				receiver.sendMessage(TextComponent.fromLegacyText(joined));
			}

		}

	}

	public void sendMessage(ProxiedPlayer sender, String message) {
		ProxyLocalCommunicationManager.sendPlayerDirectChatMessage("local", sender.getName(), message, sender.getServer().getInfo());
	}

	public void broadcastRawMessage(CommandSender sender, String server, String message) {

		message = MultiChatUtil.reformatRGB(message);
		message = ChatColor.translateAlternateColorCodes('&', message);

		for (ProxiedPlayer receiver : ProxyServer.getInstance().getPlayers()) {

			// Skip sending to this player if they shouldn't receive the message
			if (receiver.getServer() == null // Receiver is between servers
					|| manager.isHidden(receiver.getUniqueId(), "local")) // Receiver has hidden this channel
				continue;

			// If not on specified server then return
			if (!receiver.getServer().getInfo().getName().equals(server)) continue;

			if (MultiChat.legacyServers.contains(receiver.getServer().getInfo().getName())) {
				receiver.sendMessage(TextComponent.fromLegacyText(MultiChatUtil.approximateHexCodes(message)));
			} else {
				receiver.sendMessage(TextComponent.fromLegacyText(message));
			}

		}

	}

}