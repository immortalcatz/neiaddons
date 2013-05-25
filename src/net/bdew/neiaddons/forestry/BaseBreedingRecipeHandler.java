/**
 * Copyright (c) bdew, 2013
 * https://github.com/bdew/neiaddons
 *
 * This mod is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * https://raw.github.com/bdew/neiaddons/master/MMPL-1.0.txt
 */

package net.bdew.neiaddons.forestry;

import java.awt.Rectangle;
import java.util.ArrayList;

import net.bdew.neiaddons.Utils;
import net.bdew.neiaddons.forestry.fake.FakeSpeciesRoot;
import net.bdew.neiaddons.utils.LabeledPositionedStack;
import net.minecraft.item.ItemStack;
import codechicken.nei.PositionedStack;
import codechicken.nei.forge.GuiContainerManager;
import codechicken.nei.recipe.TemplateRecipeHandler;
import forestry.api.genetics.IAlleleSpecies;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.IMutation;

public abstract class BaseBreedingRecipeHandler extends TemplateRecipeHandler {

    private final FakeSpeciesRoot speciesRoot;

    public BaseBreedingRecipeHandler(FakeSpeciesRoot root) {
        this.speciesRoot = root;
    }

    public class CachedBreedingRecipe extends CachedRecipe {
        LabeledPositionedStack parrent1, parrent2, result;
        public float chance;

        public CachedBreedingRecipe(IMutation mutation) {

            ItemStack stackParent1 = GeneticsUtils.stackFromSpecies((IAlleleSpecies) mutation.getAllele0(), GeneticsUtils.RecipePosition.Parent1);
            ItemStack stackParent2 = GeneticsUtils.stackFromSpecies((IAlleleSpecies) mutation.getAllele1(), GeneticsUtils.RecipePosition.Parent2);
            ItemStack stackResult = GeneticsUtils.stackFromSpecies((IAlleleSpecies) mutation.getTemplate()[0], GeneticsUtils.RecipePosition.Offspring);

            parrent1 = new LabeledPositionedStack(stackParent1, 22, 19, ((IAlleleSpecies) mutation.getAllele0()).getName(), 13);
            parrent2 = new LabeledPositionedStack(stackParent2, 75, 19, ((IAlleleSpecies) mutation.getAllele1()).getName(), 13);
            result = new LabeledPositionedStack(stackResult, 129, 19, ((IAlleleSpecies) mutation.getTemplate()[0]).getName(), 13);
            chance = mutation.getBaseChance();
        }

        @Override
        public PositionedStack getResult() {
            return result;
        }

        @Override
        public ArrayList<PositionedStack> getIngredients() {
            ArrayList<PositionedStack> list = new ArrayList<PositionedStack>();
            list.add(parrent1);
            list.add(parrent2);
            return list;
        }
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals("item")) {
            loadCraftingRecipes((ItemStack) results[0]);
            return;
        }

        if (!outputId.equals(getRecipeIdent())) { return; }

        for (IMutation mutation : speciesRoot.getMutations(false)) {
            if (!mutation.isSecret() || AddonForestry.showSecret) {
                arecipes.add(new CachedBreedingRecipe(mutation));
            }
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        if (!speciesRoot.isMember(result)) { return; }
        IIndividual resultIndividual = speciesRoot.getMember(result);
        IAlleleSpecies species = resultIndividual.getGenome().getPrimary();

        for (IMutation mutation : speciesRoot.getMutations(false)) {
            if (mutation.getTemplate()[0].equals(species)) {
                if (!mutation.isSecret() || AddonForestry.showSecret) {
                    arecipes.add(new CachedBreedingRecipe(mutation));
                }
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        if (!speciesRoot.isMember(ingredient)) { return; }
        IIndividual individual = speciesRoot.getMember(ingredient);
        IAlleleSpecies species = individual.getGenome().getPrimary();

        for (IMutation mutation : speciesRoot.getMutations(false)) {
            if (mutation.getAllele0().equals(species) || mutation.getAllele1().equals(species)) {
                if (!mutation.isSecret() || AddonForestry.showSecret) {
                    arecipes.add(new CachedBreedingRecipe(mutation));
                }
            }
        }
    }

    @Override
    public void loadTransferRects() {
        transferRects.add(new RecipeTransferRect(new Rectangle(49, 26, 15, 15), getRecipeIdent()));
        transferRects.add(new RecipeTransferRect(new Rectangle(98, 26, 21, 18), getRecipeIdent()));
    }

    @Override
    public void drawExtras(GuiContainerManager gui, int recipe) {
        CachedBreedingRecipe rec = (CachedBreedingRecipe) arecipes.get(recipe);
        rec.result.drawLabel(gui.window.fontRenderer);
        rec.parrent1.drawLabel(gui.window.fontRenderer);
        rec.parrent2.drawLabel(gui.window.fontRenderer);
        Utils.drawCenteredString(gui.window.fontRenderer, String.format("%.0f%%", rec.chance), 108, 15, 0xFFFFFF);
    }

    public abstract String getRecipeIdent();

    @Override
    public String getGuiTexture() {
        return "/mods/neiaddons/textures/gui/breeding.png";
    }
}
