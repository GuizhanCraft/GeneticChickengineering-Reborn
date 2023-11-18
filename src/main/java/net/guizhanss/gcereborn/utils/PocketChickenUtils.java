package net.guizhanss.gcereborn.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.bukkit.entity.Chicken;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;

import net.guizhanss.gcereborn.GeneticChickengineering;
import net.guizhanss.gcereborn.core.genetics.DNA;
import net.guizhanss.gcereborn.items.GCEItems;
import net.guizhanss.gcereborn.items.chicken.ChickenTypes;
import net.guizhanss.gcereborn.items.chicken.PocketChicken;
import net.guizhanss.gcereborn.setup.Groups;
import net.guizhanss.gcereborn.setup.RecipeTypes;

import lombok.experimental.UtilityClass;

/**
 * Utility class for {@link PocketChicken}.
 */
@UtilityClass
public final class PocketChickenUtils {

    /**
     * Determine whether an {@link ItemStack} is a {@link PocketChicken}.
     *
     * @param item
     *     The {@link ItemStack} to check.
     *
     * @return Whether the {@link ItemStack} is a {@link PocketChicken}.
     */
    public boolean isPocketChicken(@Nonnull ItemStack item) {
        return item != null && !item.getType().isAir() && item.hasItemMeta()
            && PersistentDataAPI.hasIntArray(item.getItemMeta(), Keys.DNA);
    }

    /**
     * Get a json object representing a baby chicken.
     *
     * @return A json object representing a baby chicken.
     */
    @Nonnull
    public static JsonObject getBabyJson() {
        JsonObject json = new JsonObject();
        json.addProperty("_type", "CHICKEN");
        json.addProperty("_health", 4.0);
        json.addProperty("_absorption", 0.0);
        json.addProperty("_removeWhenFarAway", false);
        json.addProperty("_customName", (String) null);
        json.addProperty("_customNameVisible", false);
        json.addProperty("_ai", true);
        json.addProperty("_silent", false);
        json.addProperty("_glowing", false);
        json.addProperty("_invulnerable", false);
        json.addProperty("_collidable", true);
        json.addProperty("_gravity", true);
        json.addProperty("_fireTicks", 0);
        json.addProperty("baby", true);
        json.addProperty("_age", -24000);
        json.addProperty("_ageLock", false);
        json.addProperty("_breedable", false);
        json.addProperty("_loveModeTicks", 0);
        JsonObject attributes = new JsonObject();
        json.add("_attributes", attributes);
        JsonObject effects = new JsonObject();
        json.add("_effects", effects);
        JsonArray tags = new JsonArray();
        json.add("_scoreboardTags", tags);
        return json;
    }

    /**
     * Captures a {@link Chicken} and returns a pocket chicken item.
     *
     * @param chicken
     *     The {@link Chicken} to capture.
     *
     * @return The pocket chicken item.
     */
    @Nonnull
    public static ItemStack capture(@Nonnull Chicken chicken) {
        GeneticChickengineering.getIntegrationService().captureChicken(chicken);
        JsonObject json = PocketChicken.ADAPTER.saveData(chicken);
        ItemStack item = GCEItems.POCKET_CHICKEN.clone();
        var db = GeneticChickengineering.getDatabaseService();

        DNA dna;
        String uuid = chicken.getUniqueId().toString();

        if (chicken.hasMetadata(Keys.METADATA)) {
            String dnaStr = chicken.getMetadata(Keys.METADATA).get(0).asString();
            GeneticChickengineering.debug("captured chicken has meta data: {0}", dnaStr);
            dna = new DNA(dnaStr);
            db.removeChicken(uuid);
        } else if (db.hasChicken(uuid)) {
            // Checked if the UUID existed first, so null won't be returned
            String dnaStr = db.getChickenDNA(uuid);
            GeneticChickengineering.debug("captured chicken in database: {0}", dnaStr);
            dna = new DNA(dnaStr);
        } else {
            GeneticChickengineering.debug("captured chicken has no DNA information");
            dna = new DNA();
        }

        if (GeneticChickengineering.getConfigService().isDisplayResources() && json.get("_customNameVisible").getAsBoolean() && dna.isKnown()) {
            String name;
            if (!json.get("_customName").isJsonNull()) {
                name = json.get("_customName").getAsString();
            } else {
                name = "";
            }
            String replace = "(" + ChickenTypes.getDisplayName(dna.getTyping()) + ")";
            name = name.replace(replace, "");
            if (name.isEmpty()) {
                json.addProperty("_customName", (String) null);
                json.addProperty("_customNameVisible", false);
            } else {
                json.addProperty("_customName", name);
            }
        }

        setLore(item, json, dna);
        return item;
    }

    /**
     * Try to breed two chicken.
     *
     * @param chick1
     *     The first chicken.
     * @param chick2
     *     The second chicken.
     *
     * @return The resulting baby chicken, or null if breeding failed.
     */
    @Nullable
    @ParametersAreNonnullByDefault
    public static ItemStack breed(ItemStack chick1, ItemStack chick2) {
        Preconditions.checkArgument(chick1 != null, "chick1 cannot be null");
        Preconditions.checkArgument(chick2 != null, "chick2 cannot be null");

        ItemMeta c1m = chick1.getItemMeta();
        ItemMeta c2m = chick2.getItemMeta();
        if (PersistentDataAPI.hasIntArray(c1m, Keys.DNA) && PersistentDataAPI.hasIntArray(c2m, Keys.DNA)) {
            DNA c1d = new DNA(PersistentDataAPI.getIntArray(c1m, Keys.DNA));
            DNA c2d = new DNA(PersistentDataAPI.getIntArray(c2m, Keys.DNA));
            return fromDNA(new DNA(c1d.split(), c2d.split()));
        }
        return null;
    }

    /**
     * Creates a display item for the given product in the dictionary.
     *
     * @param typing
     *     The type of chicken.
     */
    public static void createProductDisplay(int typing) {
        ItemStack fake = GCEItems.POCKET_CHICKEN.clone();
        DNA dna = new DNA(typing);
        String productRawName = ChickenTypes.getName(typing);
        setLore(fake, null, dna);

        // Use the chicken's resource as the icon
        String itemIDType = productRawName.replace(" ", "_").toUpperCase();
        SlimefunItemStack displayItem = new SlimefunItemStack("GCE_" + itemIDType + "_CHICKEN_ICON",
            ChickenTypes.getProduct(typing));
        // Since these will be "Pocket Chickens", they will spawn chickens when cheated into a player's inventory
        // We set the DNA on the icon so that it will spawn a chicken of the correct type
        ItemMeta meta = displayItem.getItemMeta();
        PersistentDataAPI.setIntArray(meta, Keys.DNA, dna.getState());
        displayItem.setItemMeta(meta);

        // Register the display
        new PocketChicken(
            Groups.DICTIONARY,
            displayItem,
            RecipeTypes.FROM_CHICKEN,
            new ItemStack[] {
                null, null, null,
                null, fake, null,
                null, null, null
            }
        ).register(GeneticChickengineering.getInstance());
    }

    /**
     * Create a fresh new chicken based on the DNA.
     *
     * @param dna
     *     The DNA to use.
     *
     * @return The new chicken item.
     */
    @Nonnull
    public static ItemStack fromDNA(@Nonnull DNA dna) {
        JsonObject json = getBabyJson();

        ItemStack item = GCEItems.POCKET_CHICKEN.clone();
        setLore(item, json, dna);
        return item;
    }

    @Nonnull
    public static DNA getDNA(@Nonnull ItemStack chicken) {
        ItemMeta meta = chicken.getItemMeta();
        return new DNA(PersistentDataAPI.getIntArray(meta, Keys.DNA));
    }

    /**
     * Returns a number which reflects the number of homozygous dominant alleles in a chicken.
     * This is used to give a boosted rate to resource production from chickens which are "pure".
     *
     * @param chicken
     *     The chicken {@link ItemStack}.
     *
     * @return The DNA strength.
     */
    public static int getDNAStrength(@Nonnull ItemStack chicken) {
        DNA dna = getDNA(chicken);
        int[] state = dna.getState();
        int str = 6 - dna.getTier();
        for (int i = 0; i < 6; i++) {
            if (state[i] == 1) {
                str--;
            }
        }
        return str;
    }

    @Nonnull
    private static List<String> getLore(@Nullable JsonObject json, @Nonnull DNA dna) {
        List<String> lore = new LinkedList<>();
        var localization = GeneticChickengineering.getLocalization();
        if (json != null) {
            lore = PocketChicken.ADAPTER.getLore(json);
            if (GeneticChickengineering.getConfigService().isPainEnabled()) {
                double health = json.get("_health").getAsDouble();
                String status;
                if (health > 2.0) {
                    status = localization.getString("lores.chicken.status.healthy");
                } else if (health <= 0.50) {
                    status = localization.getString("lores.chicken.status.exhausted");
                } else {
                    status = localization.getString("lores.chicken.status.fatigued");
                }
                lore.add(localization.getString("lores.chicken.status.line", status));
            }
        }
        if (dna.isKnown()) {
            lore.add(localization.getString("lores.chicken.dna", dna));
            lore.add(localization.getString("lores.chicken.type", ChickenTypes.getDisplayName(dna.getTyping())));
        }
        return lore;
    }

    public static void setLore(@Nonnull ItemStack item, @Nullable JsonObject json, @Nonnull DNA dna) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataAPI.setIntArray(meta, Keys.DNA, dna.getState());
        if (json != null) {
            PersistentDataAPI.set(meta, Keys.ADAPTER, PocketChicken.ADAPTER, json);
        }
        meta.setLore(getLore(json, dna));

        item.setItemMeta(meta);
    }

    public double getHealth(@Nullable ItemStack chicken) {
        if (chicken == null || chicken.getType().isAir()) {
            return 0d;
        }
        ItemMeta meta = chicken.getItemMeta();
        JsonObject json = PersistentDataAPI.get(meta, Keys.ADAPTER, PocketChicken.ADAPTER);
        if (json != null) {
            return json.get("_health").getAsDouble();
        }
        return 0d;
    }

    public boolean survivesPain(@Nullable ItemStack chicken) {
        return getHealth(chicken) > 0.25;
    }

    public boolean harm(@Nullable ItemStack chicken) {
        return harm(chicken, 0.25);
    }

    public boolean harm(@Nullable ItemStack chicken, double amount) {
        if (chicken == null || chicken.getType().isAir()) {
            return false;
        }
        ItemMeta meta = chicken.getItemMeta();
        JsonObject json = PersistentDataAPI.get(meta, Keys.ADAPTER, PocketChicken.ADAPTER);
        if (json != null) {
            double oldHealth = json.get("_health").getAsDouble();
            double newHealth = Math.max(0d, Math.min(oldHealth - amount, 4d));
            // Adding existing properties overwrites them
            json.addProperty("_health", newHealth);
            setLore(chicken, json, getDNA(chicken));
            return true;
        }
        return false;
    }

    public boolean heal(@Nullable ItemStack chicken, double amount) {
        if (amount > 0) {
            amount *= -1;
        }
        return harm(chicken, amount);
    }

    public void possiblyHarm(@Nullable ItemStack chicken) {
        if (ThreadLocalRandom.current().nextInt(100) < GeneticChickengineering.getConfigService().getPainChance()) {
            harm(chicken);
        }
    }

    @Nonnull
    public ItemStack getResource(@Nonnull ItemStack chicken) {
        DNA dna = getDNA(chicken);
        return ChickenTypes.getProduct(dna.getTyping());
    }

    public int getResourceTier(@Nonnull ItemStack chicken) {
        // Returns the number of homozygous recessive genes in the chicken
        // which represents the difficulty of obtaining this chicken
        DNA dna = getDNA(chicken);
        return dna.getTier();
    }

    /**
     * Determines whether the chicken is an adult.
     *
     * @param chicken
     *     The chicken {@link ItemStack}.
     *
     * @return Whether the chicken is an adult.
     */
    public boolean isAdult(@Nonnull ItemStack chicken) {
        JsonObject json = PersistentDataAPI.get(chicken.getItemMeta(), Keys.ADAPTER, PocketChicken.ADAPTER);
        if (json != null) {
            return !json.get("baby").getAsBoolean();
        }
        return false;
    }

    /**
     * Determines whether the DNA of chicken is known.
     *
     * @param chicken
     *     The chicken {@link ItemStack}.
     *
     * @return Whether the DNA is known.
     */
    public boolean isLearned(@Nonnull ItemStack chicken) {
        DNA dna = getDNA(chicken);
        return dna.isKnown();
    }

    /**
     * Learn the DNA of a chicken. This returns a new {@link ItemStack} with known DNA.
     *
     * @param chicken
     *     The chicken {@link ItemStack}.
     *
     * @return The new chicken {@link ItemStack}.
     */
    @Nonnull
    public ItemStack learnDNA(@Nonnull ItemStack chicken) {
        ItemStack item = chicken.clone();
        ItemMeta meta = item.getItemMeta();

        if (PersistentDataAPI.hasIntArray(meta, Keys.DNA)) {
            DNA dna = new DNA(PersistentDataAPI.getIntArray(meta, Keys.DNA));
            dna.learn();
            JsonObject json = PersistentDataAPI.get(meta, Keys.ADAPTER, PocketChicken.ADAPTER);
            setLore(item, json, dna);
        }

        return item;
    }
}
