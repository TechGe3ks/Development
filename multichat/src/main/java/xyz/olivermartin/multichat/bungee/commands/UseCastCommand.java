package xyz.olivermartin.multichat.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;
import xyz.olivermartin.multichat.bungee.CastControl;
import xyz.olivermartin.multichat.bungee.MessageManager;
import xyz.olivermartin.multichat.bungee.MultiChat;

/**
 * Use Cast Command
 * <p>A command designed to allow you to use a cast from the console</p>
 * 
 * @author Oliver Martin (Revilo410)
 *
 */
public class UseCastCommand extends Command {

	private static String[] aliases = new String[] {};

	public UseCastCommand() {
		super("usecast", "multichat.cast.admin", aliases);
	}

	public void displayUsage(CommandSender sender) {
		MessageManager.sendMessage(sender, "command_usecast_usage");
		sender.sendMessage(new ComponentBuilder("/usecast <name> <message>").color(ChatColor.AQUA).create());
	}

	@Override
	public void execute(CommandSender sender, String[] args) {

		if (args.length < 2) {
			displayUsage(sender);
			return;
		}

		if (CastControl.existsCast(args[0])) {

			boolean starter = false;
			String message = "";
			for (String part : args) {
				if (!starter) {
					starter = true;
				} else {
					message = message + part + " ";
				}
			}

			CastControl.sendCast(args[0],message,MultiChat.globalChat);

		} else {

			MessageManager.sendSpecialMessage(sender, "command_usecast_does_not_exist", args[0].toUpperCase());
			return;

		}
	}
}