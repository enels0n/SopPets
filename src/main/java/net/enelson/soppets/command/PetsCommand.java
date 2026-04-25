package net.enelson.soppets.command;

import net.enelson.soppets.SopPetsPlugin;
import net.enelson.soppets.model.PetDefinition;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class PetsCommand implements CommandExecutor {
    private final SopPetsPlugin plugin;

    public PetsCommand(SopPetsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("soppets.admin")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission.");
                return true;
            }
            this.plugin.reloadPlugin();
            sender.sendMessage(ChatColor.GREEN + "SopPets reloaded.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can manage pets.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("soppets.use")) {
            player.sendMessage(ChatColor.RED + "You do not have permission.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            player.openInventory(this.plugin.getPetMenuService().createMainMenu(player));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("hide")) {
            this.plugin.getPetSessionService().despawnPet(player.getUniqueId());
            this.plugin.getPlayerPetStorage().setActivePet(player.getUniqueId(), null);
            player.sendMessage(ChatColor.YELLOW + "Pet hidden.");
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("summon")) {
            PetDefinition definition = this.plugin.getPetDefinitionService().getPet(args[1]);
            if (definition == null) {
                player.sendMessage(ChatColor.RED + "Unknown pet.");
                return true;
            }
            String error = this.plugin.getPetSessionService().summonPet(player, definition.getId());
            if (error != null) {
                player.sendMessage(error);
                return true;
            }
            player.sendMessage(ChatColor.GREEN + "Pet summoned: " + definition.getDisplayName());
            return true;
        }

        player.sendMessage(ChatColor.YELLOW + "/pets list");
        player.sendMessage(ChatColor.YELLOW + "/pets summon <id>");
        player.sendMessage(ChatColor.YELLOW + "/pets hide");
        if (player.hasPermission("soppets.admin")) {
            player.sendMessage(ChatColor.YELLOW + "/pets reload");
        }
        return true;
    }
}
