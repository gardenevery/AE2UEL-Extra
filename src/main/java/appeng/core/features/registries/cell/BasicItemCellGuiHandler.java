package appeng.core.features.registries.cell;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.*;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AEPartLocation;
import appeng.core.sync.GuiBridge;
import appeng.util.Platform;

public class BasicItemCellGuiHandler implements ICellGuiHandler {
    @Override
    public <T extends IAEStack> boolean isHandlerFor(final IStorageChannel<T> channel) {
        return channel == StorageChannels.items();
    }

    @Override
    public void openChestGui(final EntityPlayer player, final IChestOrDrive chest, final ICellHandler cellHandler,
            final IMEInventoryHandler inv, final ItemStack is, final IStorageChannel chan) {
        Platform.openGUI(player, (TileEntity) chest, AEPartLocation.fromFacing(chest.getUp()), GuiBridge.GUI_ME);
    }
}
