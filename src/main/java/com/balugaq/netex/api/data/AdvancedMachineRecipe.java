package com.balugaq.netex.api.data;


import com.ytdd9527.networksexpansion.utils.itemstacks.CompareUtil;
import lombok.Getter;

import javax.annotation.Nonnull;

import com.ytdd9527.networksexpansion.utils.itemstacks.CompareUtil;

/**
 * @author Final_ROOT
 * @since 2.0
 */
public class AdvancedMachineRecipe {
    @Nonnull
    private final ItemAmountWrapper[] inputs;
    @Nonnull
    private final AdvancedRandomOutput[] randomOutputs;
    private final int[] weightBeginValues;
    @Getter
    private int weightSum = 0;

    public AdvancedMachineRecipe(@Nonnull ItemAmountWrapper[] inputs, @Nonnull AdvancedRandomOutput[] randomOutputs) {
        this.inputs = inputs;
        this.randomOutputs = randomOutputs;
        this.weightBeginValues = new int[randomOutputs.length];
        for (int i = 0; i < this.randomOutputs.length; i++) {
            this.weightBeginValues[i] = this.weightSum;
            this.weightSum += this.randomOutputs[i].getWeight();
        }
    }

    @Nonnull
    public ItemAmountWrapper[] getInput() {
        return this.inputs;
    }

    @Nonnull
    public ItemAmountWrapper[] getOutput() {
        int r = (int) (Math.random() * this.weightSum);
        return this.randomOutputs[CompareUtil.getIntSmallFuzzyIndex(this.weightBeginValues, r)].outputItem;
    }

    @Nonnull
    public AdvancedRandomOutput[] getOutputs() {
        return this.randomOutputs;
    }

    public boolean isRandomOutput() {
        return this.randomOutputs.length > 1;
    }

    public record AdvancedRandomOutput(@Nonnull ItemAmountWrapper[] outputItem, int weight) {

        @Nonnull
        public ItemAmountWrapper[] getOutputItem() {
            return outputItem;
        }

        public int getWeight() {
            return weight;
        }
    }
}
