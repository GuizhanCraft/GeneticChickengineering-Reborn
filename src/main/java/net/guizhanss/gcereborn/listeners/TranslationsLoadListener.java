package net.guizhanss.gcereborn.listeners;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.guizhanss.gcereborn.GeneticChickengineering;
import net.guizhanss.slimefuntranslation.api.events.TranslationsLoadEvent;

public class TranslationsLoadListener implements Listener {

    @ParametersAreNonnullByDefault
    public TranslationsLoadListener(@Nonnull GeneticChickengineering plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onTranslationsLoad(@Nonnull TranslationsLoadEvent e) {
        GeneticChickengineering.getScheduler().runAsync(() -> GeneticChickengineering.getIntegrationService().loadTranslations());
    }
}
