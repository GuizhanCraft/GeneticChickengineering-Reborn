package net.guizhanss.gcereborn.items.chicken;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;

import net.guizhanss.gcereborn.GeneticChickengineering;

import lombok.Getter;

@Getter
public class ChickenProduct {

    private final String name;
    private final ItemStack product;

    @ParametersAreNonnullByDefault
    public ChickenProduct(String name, ItemStack product) {
        this.name = name.toUpperCase(Locale.ROOT);
        this.product = product;
    }

    @ParametersAreNonnullByDefault
    public ChickenProduct(ItemStack product) {
        this(product.getType().name(), product);
    }

    @ParametersAreNonnullByDefault
    public ChickenProduct(String name, SlimefunItemStack product) {
        this(name, product.item());
    }

    @ParametersAreNonnullByDefault
    public ChickenProduct(SlimefunItemStack product) {
        this(product.getItemId(), product.item());
    }

    @Nonnull
    public String getProductName() {
        return GeneticChickengineering.getLocalization().getString("products." + name);
    }
}
