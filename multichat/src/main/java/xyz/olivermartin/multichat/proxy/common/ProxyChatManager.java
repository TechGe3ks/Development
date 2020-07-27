package xyz.olivermartin.multichat.proxy.common;

import java.util.Optional;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import xyz.olivermartin.multichat.bungee.ChatControl;
import xyz.olivermartin.multichat.bungee.DebugManager;
import xyz.olivermartin.multichat.bungee.MessageManager;

public class ProxyChatManager {

	public ProxyChatManager() {
		/* EMPTY */
	}

	/**
	 * Check if the player has permission to use simple colour codes in chat
	 * @param player The player to check
	 * @return true if they have permission
	 */
	public boolean hasSimpleColourPermission(ProxiedPlayer player) {
		return player.hasPermission("multichat.chat.colour.simple")
				|| player.hasPermission("multichat.chat.color.simple")
				|| hasRGBColourPermission(player);
	}

	/**
	 * Check if the player has permission to use RGB colour codes in chat
	 * @param player The player to check
	 * @return true if they have permission
	 */
	public boolean hasRGBColourPermission(ProxiedPlayer player) {
		return player.hasPermission("multichat.chat.colour.rgb")
				|| player.hasPermission("multichat.chat.color.rgb")
				|| hasLegacyColourPermission(player);
	}

	/**
	 * Check if the player has permission to use all colour codes in chat (legacy permission)
	 * @param player The player to check
	 * @return true if they have permission
	 */
	public boolean hasLegacyColourPermission(ProxiedPlayer player) {
		return player.hasPermission("multichat.chat.colour")
				|| player.hasPermission("multichat.chat.color");
	}

	/**
	 * <p>Check if this player is allowed to send a chat message</p>
	 * <p>Possible reasons for not being allowed are:
	 * <ul>
	 *     <li>Chat is frozen</li>
	 *     <li>Player is muted by MultiChat</li>
	 *     <li>Player is spamming</li>
	 * </ul>
	 * </p>
	 * @param player The player to check
	 * @param message The message they are trying to send
	 * @return true if they are allowed to send a message
	 */
	public boolean canPlayerSendChat(ProxiedPlayer player, String message) {

		// Check if chat is frozen
		if (MultiChatProxy.getInstance().getDataStore().isChatFrozen() && !player.hasPermission("multichat.chat.always")) {
			MessageManager.sendMessage(player, "freezechat_frozen");
			return false;
		}

		// Check if they are muted by MultiChat
		if (ChatControl.isMuted(player.getUniqueId(), "global_chat")) {
			MessageManager.sendMessage(player, "mute_cannot_send_message");
			return false;
		}

		// Check if they are spamming
		if (ChatControl.handleSpam(player, message, "global_chat")) {
			DebugManager.log(player.getName() + " - chat message being cancelled due to spam");
			return false;
		}

		return true;

	}

	/**
	 * <p>Pre-processes a chat message before sending it</p>
	 * <p>This includes the following:
	 * <ul>
	 *     <li>Applying regex rules and actions</li>
	 *     <li>Filtering links if they player does not have permission</li>
	 * </ul>
	 * </p>
	 * @param player The player to check
	 * @param message The message they are trying to send
	 * @return the new processed string if they are allowed to send the message, or empty if the message should be cancelled
	 */
	public Optional<String> preProcessMessage(ProxiedPlayer player, String message) {

		Optional<String> crm;

		crm = ChatControl.applyChatRules(message, "global_chat", player.getName());

		if (crm.isPresent()) {
			message = crm.get();
		} else {
			return Optional.empty();
		}

		if (!player.hasPermission("multichat.chat.link")) {
			message = ChatControl.replaceLinks(message);
		}

		return Optional.of(message);

	}

}
