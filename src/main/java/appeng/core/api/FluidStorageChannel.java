package appeng.core.api;

import java.io.IOException;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AppEng;
import appeng.fluids.items.FluidDummyItem;
import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.FluidList;

public class FluidStorageChannel implements IFluidStorageChannel {

    public static final IFluidStorageChannel INSTANCE = new FluidStorageChannel();

    private FluidStorageChannel() {
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return AppEng.makeId("fluid");
    }

    @Override
    public int transferFactor() {
        return 1000;
    }

    @Override
    public int getUnitsPerByte() {
        return 8000;
    }

    @Override
    public IItemList<IAEFluidStack> createList() {
        return new FluidList();
    }

    @Override
    public IAEFluidStack createStack(Object input) {
        Preconditions.checkNotNull(input);

        if (input instanceof FluidStack stack) {
            return AEFluidStack.fromFluidStack(stack);
        }
        if (input instanceof ItemStack is) {
            if (is.getItem() instanceof FluidDummyItem) {
                return AEFluidStack.fromFluidStack(((FluidDummyItem) is.getItem()).getFluidStack(is));
            } else {
                return AEFluidStack.fromFluidStack(FluidUtil.getFluidContained(is));
            }
        }

        return null;
    }

    @Override
    public IAEFluidStack readFromPacket(ByteBuf input) throws IOException {
        Preconditions.checkNotNull(input);

        return AEFluidStack.fromPacket(input);
    }

    @Override
    public IAEFluidStack createFromNBT(NBTTagCompound nbt) {
        Preconditions.checkNotNull(nbt);
        return AEFluidStack.fromNBT(nbt);
    }

    @Override
    public IAEFluidStack copy(IAEFluidStack stack) {
        return stack.copy();
    }
}
