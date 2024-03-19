package net.guizhanss.gcereborn.items.machines;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemUtils;

import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;

import net.guizhanss.gcereborn.GeneticChickengineering;
import net.guizhanss.gcereborn.items.GCEItems;
import net.guizhanss.gcereborn.utils.PocketChickenUtils;

public class RestorationChamber extends AbstractMachine {

    public RestorationChamber(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    @Nonnull
    public ItemStack getProgressBar() {
        return GCEItems.POCKET_CHICKEN.clone();
    }

    @Override
    @Nullable
    protected MachineRecipe findNextRecipe(@Nonnull BlockMenu menu) {
        var config = GeneticChickengineering.getConfigService();
        ItemStack chicken = null;
        ItemStack seed = null;
        for (int slot : getInputSlots()) {
            ItemStack item = menu.getItemInSlot(slot);

            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            if (PocketChickenUtils.isPocketChicken(item)) {
                chicken = item;
            } else if (item.getType() == Material.WHEAT_SEEDS) {
                seed = item;
            }
        }

        if (chicken == null || seed == null) {
            return null;
        }

        double health = PocketChickenUtils.getHealth(chicken);
        int seedAmount = seed.getAmount();
        int toConsume = 0;
        while (seedAmount > 0 && health < 4d) {
            seedAmount--;
            toConsume++;
            health = health + 0.25;
        }
        if (toConsume == 0) {
            return null;
        }
        ItemStack recipeSeeds = seed.clone();
        recipeSeeds.setAmount(toConsume);
        ItemStack recipeChick = chicken.clone();
        PocketChickenUtils.heal(recipeChick, toConsume * 0.25);
        var healRate = GeneticChickengineering.getConfigService().getHealRate();
        MachineRecipe recipe = new MachineRecipe(
            config.isTest() ? 1 : healRate * toConsume,
            new ItemStack[] {recipeSeeds, chicken.clone()},
            new ItemStack[] {recipeChick}
        );
        if (!InvUtils.fitAll(menu.toInventory(), recipe.getOutput(), getOutputSlots())) {
            return null;
        }
        ItemUtils.consumeItem(chicken, false);
        ItemUtils.consumeItem(seed, toConsume, false);
        return recipe;
    }
}
