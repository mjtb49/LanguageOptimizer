package io.github.mjtb49.languageoptimizer.mixin;

import io.github.mjtb49.languageoptimizer.LanguageOptimizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.resource.language.LanguageDefinition;
import net.minecraft.client.search.SearchManager;
import net.minecraft.client.search.SearchableContainer;
import net.minecraft.item.Item;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow @Final private SearchManager searchManager;

    @Shadow @Nullable public ClientPlayerEntity player;

    private boolean isRequired(Item item) {
        return LanguageOptimizer.REQUIRED_ITEMS.contains(item); //|| item instanceof BedItem;
    }

    private boolean isDesired(Item item) {
        return LanguageOptimizer.DESIRABLE_ITEMS.containsKey(item);
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {

        try {
            SearchableContainer<RecipeResultCollection> searchableContainer = searchManager.get(SearchManager.RECIPE_OUTPUT);

            if (!LanguageOptimizer.HAS_FIRED && searchableContainer.findAll("").size() > 0) {
                LanguageDefinition mainLanguage = MinecraftClient.getInstance().getLanguageManager().getLanguage();
                File file = new File(new SimpleDateFormat("'.\\lang_optimizer\\'yyyy'_'MM'_'dd'_'HH'_'mm'.txt'").format(new Date()));
                file.getParentFile().mkdirs();
                //TODO determine why this sometimes prints ? and �
                PrintStream printStream = new PrintStream(new FileOutputStream(file), true, "UTF-8");
                int maxLength = LanguageOptimizer.MAX_LENGTH;
                int[] mostGoodItems = new int[maxLength];
                int[] mostDecentItems = new int[maxLength];


                for (LanguageDefinition language : MinecraftClient.getInstance().getLanguageManager().getAllLanguages()) {

                    MinecraftClient.getInstance().getLanguageManager().setLanguage(language);
                    MinecraftClient.getInstance().getLanguageManager().apply(MinecraftClient.getInstance().getResourceManager());

                    searchableContainer = searchManager.get(SearchManager.RECIPE_OUTPUT);
                    searchableContainer.reload();

                    ArrayList<HashSet<String>> strings = new ArrayList<>();
                    for (int i = 0; i < maxLength; i++) {
                        strings.add(new HashSet<>());
                    }

                    searchableContainer.findAll("").forEach(recipeResultCollection -> {
                        //TODO Far faster here would be to check if the recipeResultCollections being used to generate the strings
                        //are even required / desired - and even to use only those substrings common to all of the desired recipes
                        //Only obstacle is actually telling when a recipe string is associated with a required item,
                        //which takes a few lines even below.
                        Stream<String> stringStream = recipeResultCollection.getAllRecipes().stream().flatMap((recipe) -> recipe.getOutput().getTooltip(null, TooltipContext.Default.NORMAL).stream()).map((text) -> Formatting.strip(text.getString()).trim()).filter((string) -> !string.isEmpty());
                        stringStream.forEach(r -> {
                            for (int i = 0; i < strings.size(); i++) {
                                for (int j = 0; j < r.length() - i; j++) {
                                    strings.get(i).add(r.substring(j, j + i + 1).toLowerCase(Locale.ROOT));
                                }
                            }
                        });
                    });

                    for (int length = LanguageOptimizer.MIN_LENGTH; length <= maxLength; length++) {
                        for (String searchString : strings.get(length - 1)) {

                            AtomicInteger goodCount = new AtomicInteger(0);
                            AtomicInteger decentScore = new AtomicInteger(0);
                            AtomicInteger count = new AtomicInteger(0);

                            searchableContainer.findAll(searchString.toLowerCase(Locale.ROOT)).forEach(r -> {
                                if (r.isInitialized() && r.hasCraftableRecipes() && count.get() < 20) {
                                    count.incrementAndGet();
                                    //r.getAllRecipes().forEach(recipe -> {
                                    boolean foundGoodRecipe = false;
                                    boolean foundDesiredRecipe = false;
                                    int desiredScore = 0;
                                    assert player != null;
                                    for (Recipe<?> recipe : r.getAllRecipes()) {
                                        if (player.getRecipeBook().contains(recipe) && recipe.getType() == RecipeType.CRAFTING) {
                                            if (isRequired(recipe.getOutput().getItem())) {
                                                foundGoodRecipe = true;
                                            } else if (isDesired(recipe.getOutput().getItem())) {
                                                foundDesiredRecipe = true;
                                                desiredScore = LanguageOptimizer.DESIRABLE_ITEMS.get(recipe.getOutput().getItem());
                                            }
                                        }
                                    }
                                    //    String key = recipe.getOutput().getTranslationKey();
                                    //    System.out.println((new TranslatableText(key)).getString());
                                    //});
                                    if (foundGoodRecipe)
                                        goodCount.incrementAndGet();
                                    if (foundDesiredRecipe)
                                        decentScore.addAndGet(desiredScore);
                                }
                            });
                            //TODO fix this silly condition by fixing the bed stuff
                            if (goodCount.get() >= LanguageOptimizer.REQUIRED_ITEMS.size()) {
                                mostGoodItems[length - 1] = goodCount.get();
                                if (decentScore.get() > mostDecentItems[length - 1])
                                    mostDecentItems[length - 1] = decentScore.get();
                                printStream.println("lang: " + language.getName());
                                printStream.println("more lang info: " + language + " " + language.getCode());
                                printStream.println("Search: \"" + searchString + "\"");
                                printStream.println("Num required items found " + goodCount);
                                printStream.println("Total weight of desired items " + decentScore);
                                printStream.println("String length " + length);
                                printStream.println();
                            }
                        }
                    }
                }

                assert player != null;

                int maxScore = Integer.MIN_VALUE;
                for (int i = 0; i < mostGoodItems.length; i++)
                    if (mostGoodItems[i] >= LanguageOptimizer.REQUIRED_ITEMS.size()) {
                        if (mostDecentItems[i] >= maxScore)
                            maxScore = mostDecentItems[i];
                    }
                if (maxScore > Integer.MIN_VALUE)
                    player.sendMessage(new LiteralText("Goal achieved max weight of " + maxScore), false);

                player.sendMessage(new LiteralText("Click here for output file").formatted(Formatting.AQUA).styled(style ->
                    style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath())).setHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click Me")))), false);

                MinecraftClient.getInstance().getLanguageManager().setLanguage(mainLanguage);
                MinecraftClient.getInstance().getLanguageManager().apply(MinecraftClient.getInstance().getResourceManager());
                searchManager.get(SearchManager.RECIPE_OUTPUT).reload();
            }
            LanguageOptimizer.HAS_FIRED = true;
            //System.exit(0);
        } catch (FileNotFoundException | UnsupportedEncodingException f) {
            f.printStackTrace();
        }
    }
}
