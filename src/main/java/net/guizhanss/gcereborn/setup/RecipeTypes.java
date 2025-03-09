package net.guizhanss.gcereborn.setup;

import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;

import net.guizhanss.gcereborn.GeneticChickengineering;
import net.guizhanss.gcereborn.items.GCEItems;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class RecipeTypes {

    public static final RecipeType FROM_NET = GeneticChickengineering.getLocalization().getRecipeType(
        "from_net",
        GCEItems.CHICKEN_NET.item()
    );
    public static final RecipeType FROM_CHICKEN = GeneticChickengineering.getLocalization().getRecipeType(
        "from_chicken",
        GCEItems.EXCITATION_CHAMBER.item()
    );
}
