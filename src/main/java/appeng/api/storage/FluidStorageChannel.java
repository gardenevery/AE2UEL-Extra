package appeng.api.storage;

import java.io.IOException;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AppEng;
import appeng.fluids.items.FluidDummyItem;
import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.FluidList;

public class FluidStorageChannel implements IStorageChannel<IAEFluidStack> {

    private static final ResourceLocation ID = AppEng.makeId("fluid");

    static final FluidStorageChannel INSTANCE = new FluidStorageChannel();

    private FluidStorageChannel() {
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return ID;
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
    public IAEFluidStack createStack(ItemStack is) {
        Preconditions.checkNotNull(is, "is");

        if (is.getItem() instanceof FluidDummyItem) {
            return AEFluidStack.fromFluidStack(((FluidDummyItem) is.getItem()).getFluidStack(is));
        } else {
            FluidStack input = FluidUtil.getFluidContained(is);
            if (input == null) {
                return null;
            }
            return IAEFluidStack.of(input);
        }
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
