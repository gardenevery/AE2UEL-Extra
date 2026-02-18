package appeng.api.stacks;

import java.io.IOException;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import appeng.api.storage.AEKeyFilter;
import appeng.core.AELog;

public final class AEItemKey extends AEKey {
    private final Item item;
    @Nullable
    private final NBTTagCompound tag;
    private final int hashCode;

    private AEItemKey(Item item, @Nullable NBTTagCompound tag) {
        this.item = item;
        this.tag = tag;
        this.hashCode = Objects.hash(item, tag);
    }

    @Nullable
    public static AEItemKey of(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return of(stack.getItem(), stack.getTagCompound());
    }

    public static boolean matches(AEKey what, ItemStack itemStack) {
        return what instanceof AEItemKey itemKey && itemKey.matches(itemStack);
    }

    public static boolean is(AEKey what) {
        return what instanceof AEItemKey;
    }

    public static AEKeyFilter filter() {
        return AEItemKey::is;
    }

    @Override
    public AEKeyType getType() {
        return AEKeyType.items();
    }

    @Override
    public AEItemKey dropSecondary() {
        return of(item, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AEItemKey aeItemKey = (AEItemKey) o;
        // The hash code comparison is a fast-fail for two objects with different NBT or items
        return hashCode == aeItemKey.hashCode && item == aeItemKey.item && Objects.equals(tag, aeItemKey.tag);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public static AEItemKey of(Item item) {
        return of(item, null);
    }

    public static AEItemKey of(Item item, @Nullable NBTTagCompound tag) {
        // Do a defensive copy of the tag if we're not sure that we can take ownership
        return new AEItemKey(item, tag != null ? tag.copy() : null);
    }

    public boolean matches(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == item && Objects.equals(stack.getTagCompound(), tag);
    }

    public ItemStack toStack() {
        return toStack(1);
    }

    public ItemStack toStack(int count) {
        if (count <= 0) {
            return ItemStack.EMPTY;
        }

        var result = new ItemStack(item);
        result.setTagCompound(copyTag());
        result.setCount(count);
        return result;
    }

    public Item getItem() {
        return item;
    }

    @Nullable
    public static AEItemKey fromTag(NBTTagCompound tag) {
        try {
            String itemId = tag.getString("id");
            ResourceLocation resourceLocation = new ResourceLocation(itemId);
            Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);

            if (item == null) {
                AELog.debug("Unknown item id in NBT: %s", itemId);
                return null;
            }

            NBTTagCompound extraTag = tag.hasKey("tag") ? tag.getCompoundTag("tag") : null;
            return of(item, extraTag);
        } catch (Exception e) {
            AELog.debug("Tried to load an invalid item key from NBT: %s", tag, e);
            return null;
        }
    }

    @Override
    public NBTTagCompound toTag() {
        NBTTagCompound result = new NBTTagCompound();
        result.setString("id", ForgeRegistries.ITEMS.getKey(item).toString());

        if (tag != null) {
            result.setTag("tag", tag.copy());
        }

        return result;
    }

    @Override
    public Object getPrimaryKey() {
        return item;
    }

    /**
     * @see ItemStack#getMaxDamage()
     */
    @Override
    public int getFuzzySearchValue() {
        return this.tag == null ? 0 : this.tag.getInteger("Damage");
    }

    /**
     * @see ItemStack#getDamageValue()
     */
    @Override
    public int getFuzzySearchMaxValue() {
        return item.getMaxDamage();
    }

    @Override
    public String getModId() {
        return item.getRegistryName().getNamespace();
    }

    /**
     * @return <strong>NEVER MODIFY THE RETURNED TAG</strong>
     */
    @Nullable
    public NBTTagCompound getTag() {
        return tag;
    }

    @Nullable
    public NBTTagCompound copyTag() {
        return tag != null ? tag.copy() : null;
    }

    public boolean hasTag() {
        return tag != null;
    }

    @Override
    public ItemStack wrapForDisplayOrFilter() {
        return toStack();
    }

    @Override
    public ItemStack wrap(int amount) {
        return toStack(amount);
    }

    @Override
    public String getDisplayName() {
        return toStack().getDisplayName();
    }

    /**
     * @return True if the item represented by this key is damaged.
     */
    public boolean isDamaged() {
        return tag != null && tag.getInteger("Damage") > 0;
    }

    @Override
    public void writeToPacket(PacketBuffer data) {
        data.writeVarInt(Item.getIdFromItem(item));
        NBTTagCompound compoundTag = null;
        if (item.isDamageable() || item.getShareTag()) {
            compoundTag = tag;
        }
        data.writeCompoundTag(compoundTag);
    }

    public static AEItemKey fromPacket(PacketBuffer data) throws IOException {
        int i = data.readVarInt();
        Item item = Item.getItemById(i);
        NBTTagCompound tag = data.readCompoundTag();
        return new AEItemKey(item, tag);
    }
}
