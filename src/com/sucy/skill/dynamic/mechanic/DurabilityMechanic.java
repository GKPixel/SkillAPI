package com.sucy.skill.dynamic.mechanic;

import com.rit.sucy.version.VersionManager;
import com.sucy.skill.dynamic.EffectComponent;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * SkillAPI © 2017
 * com.sucy.skill.dynamic.mechanic.DurabilityMechanic
 */
public class DurabilityMechanic extends EffectComponent {

    private static final String AMOUNT = "amount";
    private static final String OFFHAND = "offhand";

    @Override
    public boolean execute(
            final LivingEntity caster, final int level, final List<LivingEntity> targets) {

        if (!(caster instanceof Player)) {
            return false;
        }

        final Player player = (Player)caster;
        final boolean isSelf = targets.size() == 1 && targets.get(0) == caster;
        final boolean offhand = settings.getBool(OFFHAND, false);
        final short amount = (short)(attr(caster, AMOUNT, level, 1, isSelf) * targets.size());

        final ItemStack item;
        if (offhand && VersionManager.isVersionAtLeast(VersionManager.V1_9_0)) {
            item = player.getInventory().getItemInOffHand();
        }
        else item = player.getInventory().getItemInHand();

        if (item == null || item.getType().getMaxDurability() == 0) {
            return false;
        }

        if (item.getDurability() <= amount) {
            player.getInventory().setItemInOffHand(null);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
        }
        item.setDurability((short)(item.getDurability() - amount));
        return true;
    }
}