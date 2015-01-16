package com.sucy.skill.cmd;

import com.rit.sucy.commands.CommandManager;
import com.rit.sucy.commands.ConfigurableCommand;
import com.rit.sucy.commands.IFunction;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.api.player.PlayerSkillBar;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Command to toggle on/off the skill bar
 */
public class CmdBar implements IFunction
{
    private static final String NOT_PLAYER  = "not-player";
    private static final String NO_CLASS    = "no-class";
    private static final String IN_CREATIVE = "in-creative";
    private static final String NO_SPACE    = "no-space";
    private static final String TOGGLE_ON   = "toggle-on";
    private static final String TOGGLE_OFF  = "toggle-off";

    /**
     * Executes the command
     *
     * @param command owning command
     * @param plugin  plugin reference
     * @param sender  sender of the command
     * @param args    arguments
     */
    @Override
    public void execute(ConfigurableCommand command, Plugin plugin, CommandSender sender, String[] args)
    {
        if (!(sender instanceof Player))
        {
            command.sendMessage(sender, NOT_PLAYER, "&4Only players can use this command");
            return;
        }

        PlayerData player = SkillAPI.getPlayerData((Player) sender);

        // Player must have a class
        if(!player.hasClass()){
            command.sendMessage(sender, NO_CLASS, "&4You have not professed as any class yet");
        }

        // Cannot be in creative mode
        else if (player.getPlayer().getGameMode() == GameMode.CREATIVE) {
            command.sendMessage(sender, IN_CREATIVE, "&4You cannot be in creative mode");
        }
        else {
            PlayerSkillBar bar = player.getSkillBar();

            // Not enough space
            if (!bar.isEnabled() && bar.countOpenSlots() < bar.getItemsInSkillSlots()) {
                command.sendMessage(sender, NO_SPACE, "&4You don't have enough inventory space for the skill bar");
                return;
            }

            bar.toggleEnabled();
            if (bar.isEnabled()) {
                command.sendMessage(sender, TOGGLE_ON, "&2Your skill bar has been &6enabled");
            }
            else {
                command.sendMessage(sender, TOGGLE_OFF, "&2Your skill bar has been &2disabled");
            }
        }
    }
}