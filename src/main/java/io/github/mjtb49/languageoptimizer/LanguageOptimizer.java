package io.github.mjtb49.languageoptimizer;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class LanguageOptimizer implements ModInitializer {

    public static final HashSet<Item> REQUIRED_ITEMS = new HashSet<>(Arrays.asList(
            Items.GLOWSTONE,
            Items.RESPAWN_ANCHOR,
            Items.WHITE_WOOL,
            Items.WHITE_BED
    ));

    public static final HashMap<Item, Integer> DESIRABLE_ITEMS = new HashMap<Item,Integer>(){{
                    put(Items.IRON_AXE, 1);
                    put(Items.IRON_PICKAXE, 1);
                    put(Items.BUCKET, 1);
                    put(Items.GOLDEN_CARROT, 1);
                    put(Items.GOLDEN_PICKAXE, 1);
                    put(Items.FLINT_AND_STEEL, 1);
                    put(Items.BOW, 1);
                    put(Items.FISHING_ROD, 1);
                    put(Items.IRON_INGOT, 1);
    }};

    public static boolean HAS_FIRED = true;
    public static int MIN_LENGTH = 1;
    public static int MAX_LENGTH = 3;
    @Override
    public void onInitialize() {

    }
}
