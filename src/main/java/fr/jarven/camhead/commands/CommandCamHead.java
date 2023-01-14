package fr.jarven.camhead.commands;

import org.bukkit.command.CommandSender;

import java.util.function.Predicate;

import dev.jorel.commandapi.CommandTree;
import fr.jarven.camhead.commands.camhead.CommandCamHeadAdd;
import fr.jarven.camhead.commands.camhead.CommandCamHeadConfig;
import fr.jarven.camhead.commands.camhead.CommandCamHeadInfo;
import fr.jarven.camhead.commands.camhead.CommandCamHeadLeave;
import fr.jarven.camhead.commands.camhead.CommandCamHeadList;
import fr.jarven.camhead.commands.camhead.CommandCamHeadMove;
import fr.jarven.camhead.commands.camhead.CommandCamHeadReload;
import fr.jarven.camhead.commands.camhead.CommandCamHeadRemove;
import fr.jarven.camhead.commands.camhead.CommandCamHeadReplace;
import fr.jarven.camhead.commands.camhead.CommandCamHeadRotate;
import fr.jarven.camhead.commands.camhead.CommandCamHeadSpectate;
import fr.jarven.camhead.commands.camhead.CommandCamHeadTeleportTo;

public class CommandCamHead extends CommandBuilder {
	@Override
	public CommandTree getCommandTree() {
		Predicate<CommandSender> requireMaker = s -> s != null && s.hasPermission("camhead.maker");
		Predicate<CommandSender> requireAdmin = s -> s != null && s.hasPermission("camhead.admin");
		Predicate<CommandSender> requireAdminOrMaker = s -> s != null && (s.hasPermission("camhead.maker") || s.hasPermission("camhead.admin"));
		return new CommandTree("camhead")
			.then(new CommandCamHeadAdd().getArgumentTree().withRequirement(requireMaker))
			.then(new CommandCamHeadConfig().getArgumentTree().withRequirement(requireAdmin))
			.then(new CommandCamHeadInfo().getArgumentTree().withRequirement(requireAdminOrMaker))
			.then(new CommandCamHeadLeave().getArgumentTree().withRequirement(requireAdmin))
			.then(new CommandCamHeadList().getArgumentTree().withRequirement(requireAdminOrMaker))
			.then(new CommandCamHeadMove().getArgumentTree().withRequirement(requireMaker))
			.then(new CommandCamHeadReload().getArgumentTree().withRequirement(requireAdmin))
			.then(new CommandCamHeadRemove().getArgumentTree().withRequirement(requireMaker))
			.then(new CommandCamHeadReplace().getArgumentTree().withRequirement(requireAdminOrMaker))
			.then(new CommandCamHeadRotate().getArgumentTree().withRequirement(requireMaker))
			.then(new CommandCamHeadSpectate().getArgumentTree().withRequirement(requireAdmin))
			.then(new CommandCamHeadTeleportTo().getArgumentTree().withRequirement(requireAdminOrMaker));
	}
}
