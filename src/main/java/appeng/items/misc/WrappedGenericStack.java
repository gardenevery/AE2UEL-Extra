/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.items.misc;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

import appeng.api.AEApi;
import appeng.api.definitions.IItems;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.items.AEBaseItem;

/**
 * Wraps a {@link GenericStack} in an {@link ItemStack}. Even stacks that actually represent vanilla {@link Item items}
 * will be wrapped in this item, to allow items with amount 0 to be represented as itemstacks without becoming the empty
 * item.
 */
public class WrappedGenericStack extends AEBaseItem {
    private static final String NBT_AMOUNT = "#";

    public static ItemStack wrap(GenericStack stack) {
        Objects.requireNonNull(stack, "stack");
        return wrap(stack.what(), stack.amount());
    }

    public static ItemStack wrap(AEKey what, long amount) {
        Objects.requireNonNull(what, "what");

        IItems items = AEApi.instance().definitions().items();
        var item = items.wrappedGenericStack().maybeItem().orElseThrow(IllegalStateException::new);
        var result = new ItemStack(item);

        var tag = what.toTagGeneric();
        if (amount != 0) {
            tag.setLong(NBT_AMOUNT, amount);
        }
        result.setTagCompound(tag);
        return result;
    }

    public WrappedGenericStack() {
        super();
        this.setMaxStackSize(1);
    }

    @Nullable
    public AEKey unwrapWhat(ItemStack stack) {
        if (stack.getItem() != this) {
            return null;
        }

        var tag = stack.getTagCompound();
        if (tag == null) {
            return null;
        }

        return AEKey.fromTagGeneric(tag);
    }

    public long unwrapAmount(ItemStack stack) {
        if (stack.getItem() != this) {
            return 0;
        }

        long amount = 0;
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey(NBT_AMOUNT)) {
            amount = tag.getLong(NBT_AMOUNT);
        }

        return amount;
    }

    @Override
    protected void getCheckedSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        // Don't show this item in CreativeTabs
    }
}
