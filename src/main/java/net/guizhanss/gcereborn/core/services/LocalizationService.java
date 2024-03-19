package net.guizhanss.gcereborn.core.services;

import java.io.File;
import java.text.MessageFormat;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Preconditions;

import org.bukkit.command.CommandSender;

import net.guizhanss.gcereborn.GeneticChickengineering;
import net.guizhanss.gcereborn.utils.FileUtils;
import net.guizhanss.guizhanlib.minecraft.utils.ChatUtil;
import net.guizhanss.guizhanlib.slimefun.addon.SlimefunLocalization;

@SuppressWarnings("ConstantConditions")
public final class LocalizationService extends SlimefunLocalization {
    private static final String FOLDER_NAME = "lang";
    private final GeneticChickengineering plugin;
    private final File jarFile;

    @ParametersAreNonnullByDefault
    public LocalizationService(GeneticChickengineering plugin, File jarFile) {
        super(plugin);
        this.plugin = plugin;
        this.jarFile = jarFile;
        extractTranslations();
    }

    private void extractTranslations() {
        final File translationsFolder = new File(plugin.getDataFolder(), FOLDER_NAME);
        if (!translationsFolder.exists()) {
            translationsFolder.mkdirs();
        }
        var translationFiles = FileUtils.listYmlFilesInJar(jarFile, FOLDER_NAME);
        for (String translationFile : translationFiles) {
            String filePath = FOLDER_NAME + File.separator + translationFile;
            File file = new File(plugin.getDataFolder(), filePath);
            if (file.exists()) {
                continue;
            }
            plugin.saveResource(filePath, true);
        }
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    public String getString(String key, Object... args) {
        return MessageFormat.format(getString(key), args);
    }

    @ParametersAreNonnullByDefault
    public void sendMessage(CommandSender sender, String messageKey, Object... args) {
        Preconditions.checkArgument(sender != null, "CommandSender cannot be null");
        Preconditions.checkArgument(messageKey != null, "Message key cannot be null");

        if (GeneticChickengineering.getIntegrationService().isSlimefunTranslationEnabled()) {
            GeneticChickengineering.getIntegrationService().sendMessage(sender, messageKey, args);
        } else {
            ChatUtil.send(sender, MessageFormat.format(getString("messages." + messageKey), args));
        }
    }
}
