package com.sucy.skill.cmd;

import com.rit.sucy.commands.CommandManager;
import com.rit.sucy.commands.ConfigurableCommand;
import com.rit.sucy.commands.IFunction;
import com.rit.sucy.config.Filter;
import com.rit.sucy.player.PlayerUUIDs;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.enums.ExpSource;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.language.RPGFilter;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * A command that gives a player class experience
 */
public class CmdExp implements IFunction
{
    private static final String NOT_PLAYER   = "not-player";
    private static final String NOT_NUMBER   = "not-number";
    private static final String NOT_POSITIVE = "not-positive";
    private static final String GAVE_EXP     = "gave-exp";
    private static final String RECEIVED_EXP = "received-exp";

    /**
     * Runs the command
     *
     * @param cmd    command that was executed
     * @param plugin plugin reference
     * @param sender sender of the command
     * @param args   argument list
     */
    @Override
    public void execute(ConfigurableCommand cmd, Plugin plugin, CommandSender sender, String[] args)
    {
        // Only can show info of a player so console needs to provide a name
        if (args.length >= 1 && (args.length >= 2 || sender instanceof Player))
        {
            // Get the player data
            OfflinePlayer target = args.length == 1 ? (OfflinePlayer) sender : PlayerUUIDs.getOfflinePlayer(args[0]);
            if (target == null)
            {
                cmd.sendMessage(sender, NOT_PLAYER, ChatColor.RED + "That is not a valid player name");
                return;
            }

            // Parse the experience
            double amount;
            try
            {
                amount = Double.parseDouble(args[args.length == 1 ? 0 : 1]);
            }
            catch (Exception ex)
            {
                cmd.sendMessage(sender, NOT_NUMBER, ChatColor.RED + "That is not a valid experience amount");
                return;
            }

            // Invalid amount of experience
            if (amount <= 0)
            {
                cmd.sendMessage(sender, NOT_POSITIVE, ChatColor.RED + "You must give a positive amount of experience");
                return;
            }

            // Give experience
            PlayerData data = SkillAPI.getPlayerData(target);
            data.giveExp(amount, ExpSource.COMMAND);

            // Messages
            if (target != sender)
            {
                cmd.sendMessage(sender, GAVE_EXP, ChatColor.DARK_GREEN + "You have given " + ChatColor.GOLD + "{player} {exp} experience", Filter.PLAYER.setReplacement(target.getName()), RPGFilter.EXP.setReplacement("" + amount));
            }
            if (target.isOnline())
            {
                cmd.sendMessage(target.getPlayer(), RECEIVED_EXP, ChatColor.DARK_GREEN + "You have received " + ChatColor.GOLD + "{exp} experience " + ChatColor.DARK_GREEN + "from " + ChatColor.GOLD + "{player}", Filter.PLAYER.setReplacement(sender.getName()), RPGFilter.EXP.setReplacement("" + amount));
            }
        }

        // Not enough arguments
        else
        {
            CommandManager.displayUsage(cmd, sender);
        }
    }
}
