package appeng.api.storage;

import java.io.IOException;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AppEng;
import appeng.util.item.AEItemStack;
import appeng.util.item.ItemList;

public class ItemStorageChannel implements IStorageChannel<IAEItemStack> {

    private static final ResourceLocation ID = AppEng.makeId("item");

    static final ItemStorageChannel INSTANCE = new ItemStorageChannel();

    private ItemStorageChannel() {
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public IItemList<IAEItemStack> createList() {
        return new ItemList();
    }

    @Override
    public IAEItemStack createStack(ItemStack is) {
        return IAEItemStack.of(is);
    }

    @Override
    public IAEItemStack createFromNBT(NBTTagCompound nbt) {
        Preconditions.checkNotNull(nbt);
        return AEItemStack.fromNBT(nbt);
    }

    @Override
    public IAEItemStack readFromPacket(ByteBuf input) throws IOException {
        Preconditions.checkNotNull(input);

        return AEItemStack.fromPacket(input);
    }

    @Override
    public IAEItemStack copy(IAEItemStack stack) {
        return stack.copy();
    }

}
