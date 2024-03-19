package net.guizhanss.gcereborn.core.services;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.bgsoftware.wildstacker.api.WildStackerAPI;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Chicken;

import net.guizhanss.gcereborn.GeneticChickengineering;
import net.guizhanss.gcereborn.listeners.TranslationsLoadListener;
import net.guizhanss.slimefuntranslation.api.config.TranslationConfiguration;
import net.guizhanss.slimefuntranslation.api.config.TranslationConfigurationDefaults;
import net.guizhanss.slimefuntranslation.api.config.TranslationConfigurationFields;
import net.guizhanss.slimefuntranslation.core.factories.MessageFactory;
import net.guizhanss.slimefuntranslation.utils.FileUtils;

import lombok.Getter;
import uk.antiperson.stackmob.StackMob;

@Getter
public final class IntegrationService {
    private final GeneticChickengineering plugin;

    private final boolean stackMobEnabled;
    private final boolean wildStackerEnabled;
    private final boolean slimefunTranslationEnabled;

    private StackMob stackMobInst;

    public IntegrationService(GeneticChickengineering plugin) {
        this.plugin = plugin;

        stackMobEnabled = isEnabled("StackMob");
        wildStackerEnabled = isEnabled("WildStacker");
        slimefunTranslationEnabled = isEnabled("SlimefunTranslation");

        if (stackMobEnabled) {
            stackMobInst = (StackMob) plugin.getServer().getPluginManager().getPlugin("StackMob");
        }

        if (slimefunTranslationEnabled) {
            new TranslationsLoadListener(plugin);
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
        try {
            if (stackMobEnabled) {
                var stackEntity = stackMobInst.getEntityManager().getStackEntity(chicken);
                if (stackEntity != null && stackEntity.getSize() > 1) {
                    stackEntity.incrementSize(-1);
                } else {
                    chicken.remove();
                }
            } else if (wildStackerEnabled) {
                var stackedEntity = WildStackerAPI.getStackedEntity(chicken);
                if (stackedEntity != null && stackedEntity.getStackAmount() > 1) {
                    stackedEntity.decreaseStackAmount(1, true);
                } else {
                    chicken.remove();
                }
            } else {
                chicken.remove();
            }
        } catch (Exception e) {
            GeneticChickengineering.log(Level.SEVERE, e, "An error has occurred while capturing chicken");
            chicken.remove();
        }
    }

    public void loadTranslations() {
        var fields = TranslationConfigurationFields.builder().items("items").build();
        var defaults = TranslationConfigurationDefaults.builder().name("GeneticChickengineering").prefix("GCE_").build();
        List<String> languages = FileUtils.listYamlFiles(new File(plugin.getDataFolder(), "lang"));
        for (String langFile : languages) {
            var file = new File(plugin.getDataFolder(), "lang" + File.separator + langFile);
            String lang = langFile.replace(".yml", "");
            var fileConfig = YamlConfiguration.loadConfiguration(file);
            var cfg = TranslationConfiguration.fromFileConfiguration(lang, fileConfig, fields, defaults);
            cfg.ifPresent(translationConfiguration -> translationConfiguration.register(plugin));
        }
    }

    @ParametersAreNonnullByDefault
    public void sendMessage(CommandSender sender, String messageKey, Object... args) {
        MessageFactory.get(plugin).sendMessage(sender, messageKey, args);
    }
}
