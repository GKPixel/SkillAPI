/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.ParticleProjectileMechanic
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Steven Sucy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software") to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.Settings;
import com.sucy.skill.api.particle.EffectPlayer;
import com.sucy.skill.api.particle.target.FollowTarget;
import com.sucy.skill.api.projectile.CustomProjectile;
import com.sucy.skill.api.projectile.ParticleProjectile;
import com.sucy.skill.api.projectile.ProjectileCallback;
import com.sucy.skill.api.util.ParticleHelper;
import com.sucy.skill.cast.CircleIndicator;
import com.sucy.skill.cast.CylinderIndicator;
import com.sucy.skill.cast.IIndicator;
import com.sucy.skill.cast.IndicatorType;
import com.sucy.skill.cast.ProjectileIndicator;
import com.sucy.skill.dynamic.TempEntity;
import com.sucy.skill.dynamic.target.RememberTarget;
import com.sucy.skill.util.ArmorStandUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Heals each target
 */
public class ParticleProjectileMechanic extends MechanicComponent implements ProjectileCallback {
    private static final Vector UP = new Vector(0, 1, 0);

    private static final String POSITION = "position";
    private static final String ANGLE    = "angle";
    private static final String AMOUNT   = "amount";
    private static final String LEVEL    = "skill_level";
    private static final String HEIGHT   = "height";
    private static final String RADIUS   = "rain-radius";
    private static final String SPREAD   = "spread";
    private static final String ALLY     = "group";
    private static final String RIGHT    = "right";
    private static final String UPWARD   = "upward";
    private static final String FORWARD  = "forward";

    private static final String COLLISION_RADIUS = "collision_radius";
    //Missile
    private static final String MISSILE_TARGET_KEEP_UPDATING = "missile_target_keep_updating";
    private static final String MISSILE_TARGET = "missile_target";
    private static final String MISSILE_THRESHOLD = "missile_threshold";
    private static final String MISSILE_ANGLE = "missile_angle";
    private static final String MISSILE_DELAY = "missile_delay";

    //Custom model
    private static final String USE_CUSTOM_MODEL = "use_custom_model";
    private static final String CUSTOM_MODEL_MATERIAL = "custom_model_material";
    private static final String CUSTOM_MODEL_DATA = "custom_model_data";
    private static final String CUSTOM_MODEL_NAME = "custom_model_name";
    private static final String CUSTOM_MODEL_LORE = "custom_model_lore";

    //Custom speed formula
    private static final String USE_SPEED_FORMULA = "use_speed_formula";
    private static final String SPEED_FORMULA = "speed_formula";

    //Effect
    private static final String USE_EFFECT = "use-effect";
    private static final String EFFECT_KEY = "effect-key";

    /**
     * Creates the list of indicators for the skill
     *
     * @param list   list to store indicators in
     * @param caster caster reference
     * @param targets location to base location on
     * @param level  the level of the skill to create for
     */
    @Override
    public void makeIndicators(List<IIndicator> list, Player caster, List<LivingEntity> targets, int level) {
        targets.forEach(target -> {
            // Get common values
            int amount = (int) parseValues(caster, AMOUNT, level, 1.0);
            double speed = parseValues(caster, "velocity", level, 1);
            String spread = settings.getString(SPREAD, "cone").toLowerCase();

            // Apply the spread type
            if (spread.equals("rain")) {
                double radius = parseValues(caster, RADIUS, level, 2.0);

                if (indicatorType == IndicatorType.DIM_2) {
                    IIndicator indicator = new CircleIndicator(radius);
                    indicator.moveTo(target.getLocation().add(0, 0.1, 0));
                    list.add(indicator);
                } else {
                    double height = parseValues(caster, HEIGHT, level, 8.0);
                    IIndicator indicator = new CylinderIndicator(radius, height);
                    indicator.moveTo(target.getLocation());
                    list.add(indicator);
                }
            } else {
                Vector dir = target.getLocation().getDirection();
                if (spread.equals("horizontal cone")) {
                    dir.setY(0);
                    dir.normalize();
                }
                double angle = parseValues(caster, ANGLE, level, 30.0);
                ArrayList<Vector> dirs = CustomProjectile.calcSpread(dir, angle, amount);
                Location loc = caster.getLocation().add(0, caster.getEyeHeight(), 0);
                for (Vector d : dirs) {
                    ProjectileIndicator indicator = new ProjectileIndicator(speed, 0);
                    indicator.setDirection(d);
                    indicator.moveTo(loc);
                    list.add(indicator);
                }
            }
        });
    }

    @Override
    public String getKey() {
        return "particle projectile";
    }

    /**
     * Executes the component
     *
     * @param caster  caster of the skill
     * @param level   level of the skill
     * @param targets targets to apply to
     *
     * @return true if applied to something, false otherwise
     */
    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        // Get common values
        int amount = (int) parseValues(caster, AMOUNT, level, 1.0);
        String spread = settings.getString(SPREAD, "cone").toLowerCase();
        boolean ally = settings.getString(ALLY, "enemy").toLowerCase().equals("ally");
        settings.set("level", level);

        final Settings copy = new Settings(settings);
        copy.set(ParticleProjectile.SPEED, parseValues(caster, ParticleProjectile.SPEED, level, 1), 0);
        copy.set(ParticleHelper.PARTICLES_KEY, parseValues(caster, ParticleHelper.PARTICLES_KEY, level, 1), 0);
        copy.set(ParticleHelper.RADIUS_KEY, parseValues(caster, ParticleHelper.RADIUS_KEY, level, 0), 0);

        double collisionRadius = settings.getDouble(COLLISION_RADIUS);
        //Missile
        boolean missileTargetKeepUpdating = settings.getBool(MISSILE_TARGET_KEEP_UPDATING);
        String missileTargetID = settings.getString(MISSILE_TARGET);
        double missileThreshold = settings.getDouble(MISSILE_THRESHOLD);
        double missileAngle = settings.getDouble(MISSILE_ANGLE);
        double missileDelay = settings.getDouble(MISSILE_DELAY);
        //Speed formula
        boolean useSpeedFormula = settings.getBool(USE_SPEED_FORMULA);
        String speedFormula = null;
        if(useSpeedFormula)
            speedFormula = settings.getString(SPEED_FORMULA);
        //Custom model
        ItemStack customModelItemStack = null;
        try {
            boolean useCustomModel = settings.getBool(USE_CUSTOM_MODEL);
            if(useCustomModel) {
                Material customModelMaterial = Material.valueOf(settings.getString(CUSTOM_MODEL_MATERIAL).toUpperCase().replace(" ", "_"));
                int customModelData = settings.getInt(CUSTOM_MODEL_DATA, 0);
                String customModelName = settings.getString(CUSTOM_MODEL_NAME);
                List<String> customModelLore = settings.getStringList(CUSTOM_MODEL_LORE);
                ItemStack itemStack = new ItemStack(customModelMaterial);
                ItemMeta itemMeta = itemStack.getItemMeta();

                if (SkillAPI.getSettings().useSkillModelData()) {
                    itemMeta.setCustomModelData(customModelData);
                } else {
                    itemStack.setDurability((short)customModelData);
                }
                itemMeta.setDisplayName(customModelName);
                itemMeta.setLore(customModelLore);
                itemStack.setItemMeta(itemMeta);
                customModelItemStack = itemStack;
            }

        } catch (Exception ex) {
            // Invalid or missing item material
        }
        // Fire from each target
        for (LivingEntity target : targets) {
            Location loc = target.getLocation();

            // Apply the spread type
            ArrayList<ParticleProjectile> list;
            if (spread.equals("rain")) {
                double radius = parseValues(caster, RADIUS, level, 2.0);
                double height = parseValues(caster, HEIGHT, level, 8.0);
                list = ParticleProjectile.rain(caster,
                        level, loc, copy, radius, height, amount, this,
                        collisionRadius,
                        missileTargetKeepUpdating,
                        missileTargetID,
                        missileThreshold,
                        missileAngle,
                        missileDelay,
                        customModelItemStack,
                        speedFormula);
            } else {
                Vector dir = target.getLocation().getDirection();
                if(target.getType() == EntityType.ARMOR_STAND){
                    ArmorStand armorStand = (ArmorStand) target;
                    loc = armorStand.getLocation();
                    //Bukkit.broadcastMessage("armorstand data: ");
                    //Bukkit.broadcastMessage("pitch: "+loc.getPitch()+", to radian: "+Math.toRadians(loc.getPitch()));
                    //Bukkit.broadcastMessage("yaw: "+loc.getYaw()+", to radian: "+Math.toRadians(loc.getYaw()));
                    //Bukkit.broadcastMessage("head pose euler: "+armorStand.getHeadPose().getX()+","+armorStand.getHeadPose().getY()+","+armorStand.getHeadPose().getZ());
                    loc.setYaw(loc.getYaw()+(float)Math.toDegrees(armorStand.getHeadPose().getY()));
                    loc.setPitch(loc.getPitch()+(float)Math.toDegrees(armorStand.getHeadPose().getX()));
                    Vector newDirection = loc.getDirection();
                    //Bukkit.broadcastMessage("new direction: "+newDirection.getX()+","+newDirection.getY()+","+newDirection.getZ());
                    dir = newDirection;
                    armorStand.teleport(loc);
                }

                double right = parseValues(caster, RIGHT, level, 0);
                double upward = parseValues(caster, UPWARD, level, 0);
                double forward = parseValues(caster, FORWARD, level, 0);

                Vector looking = dir.clone().setY(0).normalize();
                Vector normal = looking.clone().crossProduct(UP);
                looking.multiply(forward).add(normal.multiply(right));

                if (spread.equals("horizontal cone")) {
                    dir.setY(0);
                    dir.normalize();
                }
                double angle = parseValues(caster, ANGLE, level, 30.0);
                list = ParticleProjectile.spread(
                        caster,
                        level,
                        dir,
                        loc.add(looking).add(0, upward + 0.5, 0),
                        copy,
                        angle,
                        amount,
                        this,
                        collisionRadius,
                        missileTargetKeepUpdating,
                        missileTargetID,
                        missileThreshold,
                        missileAngle,
                        missileDelay,
                        customModelItemStack,
                        speedFormula
                );
            }

            // Set metadata for when the callback happens
            for (ParticleProjectile p : list) {
                SkillAPI.setMeta(p, LEVEL, level);
                p.setAllyEnemy(ally, !ally);
            }

            if (settings.getBool(USE_EFFECT, false)) {
                EffectPlayer player = new EffectPlayer(settings);
                for (CustomProjectile p : list) {
                    player.start(
                            new FollowTarget(p),
                            settings.getString(EFFECT_KEY, skill.getName()),
                            9999,
                            level,
                            true);
                }
            }
        }

        return targets.size() > 0;
    }

    /**
     * The callback for the projectiles that applies child components
     *
     * @param projectile projectile calling back for
     * @param hit        the entity hit by the projectile, if any
     */
    @Override
    public void callback(CustomProjectile projectile, LivingEntity hit) {
        if (hit == null) {
            hit = new TempEntity(projectile.getLocation());
        }
        ArrayList<LivingEntity> targets = new ArrayList<LivingEntity>();
        targets.add(hit);
        executeChildren(projectile.getShooter(), SkillAPI.getMetaInt(projectile, LEVEL), targets);
    }
}
