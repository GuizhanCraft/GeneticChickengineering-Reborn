package net.guizhanss.gcereborn.core.services;

import java.util.logging.Level;

import javax.annotation.Nonnull;

import org.bukkit.entity.Chicken;

import net.guizhanss.gcereborn.GeneticChickengineering;

import lombok.Getter;
import uk.antiperson.stackmob.StackMob;
import uk.antiperson.stackmob.entity.StackEntity;

@Getter
public final class IntegrationService {
    private final GeneticChickengineering plugin;

    private final boolean stackMobEnabled;
    private final boolean wildStackerEnabled;

    private StackMob stackMobInst;

    public IntegrationService(GeneticChickengineering plugin) {
        this.plugin = plugin;

        stackMobEnabled = isEnabled("StackMob");
        wildStackerEnabled = isEnabled("WildStacker");

        if (stackMobEnabled) {
            stackMobInst = (StackMob) plugin.getServer().getPluginManager().getPlugin("StackMob");
        }

    }

    private boolean isEnabled(@Nonnull String pluginName) {
        boolean result = plugin.getServer().getPluginManager().isPluginEnabled(pluginName);
        if (result) {
            GeneticChickengineering.log(Level.INFO,
                GeneticChickengineering.getLocalization().getString("console.load.integration", pluginName));
        }
        return result;
    }

    public void captureChicken(@Nonnull Chicken chicken) {
        if (stackMobEnabled) {
            StackEntity stackEntity = stackMobInst.getEntityManager().getStackEntity(chicken);
            if (stackEntity != null && stackEntity.getSize() > 1) {
                stackEntity.incrementSize(-1);
            } else {
                chicken.remove();
            }
        } else {
            chicken.remove();
        }
    }
}
