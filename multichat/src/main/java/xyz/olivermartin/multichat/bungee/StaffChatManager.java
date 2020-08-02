package xyz.olivermartin.multichat.bungee;

import java.util.Optional;

import com.olivermartin410.plugins.TChatInfo;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import xyz.olivermartin.multichat.bungee.events.PostStaffChatEvent;
import xyz.olivermartin.multichat.common.MultiChatUtil;
import xyz.olivermartin.multichat.proxy.common.MultiChatProxy;
import xyz.olivermartin.multichat.proxy.common.ProxyJsonUtils;
import xyz.olivermartin.multichat.proxy.common.ProxyUtils;
import xyz.olivermartin.multichat.proxy.common.config.ConfigFile;
import xyz.olivermartin.multichat.proxy.common.config.ConfigValues;
import xyz.olivermartin.multichat.proxy.common.storage.ProxyDataStore;

/**
 * Staff Chat Manager
 * <p>Manages chat input to the staff chats, both mod and admin</p>
 * 
 * @author Oliver Martin (Revilo410)
 */
public class StaffChatManager {

	public void sendModMessage(String username, String displayname, String server, String message) {
		sendStaffChatMessage("mod", username, displayname, server, message);
	}

	public void sendAdminMessage(String username, String displayname, String server, String message) {
		sendStaffChatMessage("admin", username, displayname, server, message);
	}

	private void sendStaffChatMessage(String id, String username, String displayname, String server, String message) {

		ProxyDataStore ds = MultiChatProxy.getInstance().getDataStore();

		ChatManipulation chatfix = new ChatManipulation();
		String messageFormat;
		if (id.equals("mod")) {
			messageFormat = ConfigManager.getInstance().getHandler(ConfigFile.CONFIG).getConfig().getString(ConfigValues.Config.ModChat.FORMAT);
		} else {
			messageFormat = ConfigManager.getInstance().getHandler(ConfigFile.CONFIG).getConfig().getString(ConfigValues.Config.AdminChat.FORMAT);
		}
		String original = message;

		Optional<String> crm;

		crm = ChatControl.applyChatRules(original, "staff_chats", username);

		if (crm.isPresent()) {
			original = crm.get();
		} else {
			return;
		}

		for (ProxiedPlayer onlineplayer : ProxyServer.getInstance().getPlayers()) {

			if (onlineplayer.hasPermission("multichat.staff." + id)) {

				if (id.equals("mod") && !ds.getModChatPreferences().containsKey(onlineplayer.getUniqueId())) {

					TChatInfo chatinfo = new TChatInfo();
					chatinfo.setChatColor(ConfigManager.getInstance().getHandler(ConfigFile.CONFIG).getConfig().getString(ConfigValues.Config.ModChat.CC_DEFAULT).toCharArray()[0]);
					chatinfo.setNameColor(ConfigManager.getInstance().getHandler(ConfigFile.CONFIG).getConfig().getString(ConfigValues.Config.ModChat.NC_DEFAULT).toCharArray()[0]);

					ds.getModChatPreferences().put(onlineplayer.getUniqueId(), chatinfo);

				} else if (id.equals("admin") && !ds.getAdminChatPreferences().containsKey(onlineplayer.getUniqueId())) {

					TChatInfo chatinfo = new TChatInfo();
					chatinfo.setChatColor(ConfigManager.getInstance().getHandler(ConfigFile.CONFIG).getConfig().getString(ConfigValues.Config.AdminChat.CC_DEFAULT).toCharArray()[0]);
					chatinfo.setNameColor(ConfigManager.getInstance().getHandler(ConfigFile.CONFIG).getConfig().getString(ConfigValues.Config.AdminChat.NC_DEFAULT).toCharArray()[0]);

					ds.getAdminChatPreferences().put(onlineplayer.getUniqueId(), chatinfo);

				}

				if (id.equals("mod")) {
					message = chatfix.replaceModChatVars(messageFormat, username, displayname, server, original, onlineplayer);
				} else {
					message = chatfix.replaceAdminChatVars(messageFormat, username, displayname, server, original, onlineplayer);
				}

				message = ProxyUtils.translateColourCodes(message);

				if (MultiChat.legacyServers.contains(onlineplayer.getServer().getInfo().getName())) {
					onlineplayer.sendMessage(ProxyJsonUtils.parseMessage(MultiChatUtil.approximateHexCodes(message)));
				} else {
					onlineplayer.sendMessage(ProxyJsonUtils.parseMessage(message));
				}

			}
		}

		// Trigger PostStaffChatEvent
		if (username.equalsIgnoreCase("console")) {
			ProxyServer.getInstance().getPluginManager().callEvent(new PostStaffChatEvent(id, ProxyServer.getInstance().getConsole() , original));
		} else {
			if (ProxyServer.getInstance().getPlayer(username) != null) {
				ProxyServer.getInstance().getPluginManager().callEvent(new PostStaffChatEvent(id, ProxyServer.getInstance().getPlayer(username) , original));
			}
		}

		if (id.equals("mod")) {
			ConsoleManager.logModChat("(" + username + ") " + original);
		} else {
			ConsoleManager.logAdminChat("(" + username + ") " + original);
		}

	}

}
