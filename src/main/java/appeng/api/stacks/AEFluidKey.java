package appeng.api.stacks;

import java.io.IOException;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.storage.AEKeyFilter;
import appeng.core.AELog;
import appeng.util.Platform;

public final class AEFluidKey extends AEKey {
    public static final int AMOUNT_BUCKET = 1000;
    public static final int AMOUNT_BLOCK = 1000;

    private final Fluid fluid;
    @Nullable
    private final NBTTagCompound tag;
    private final int hashCode;

    private AEFluidKey(Fluid Fluid, @Nullable NBTTagCompound tag) {
        this.fluid = Fluid;
        this.tag = tag;
        this.hashCode = Objects.hash(Fluid, tag);
    }

    public static AEFluidKey of(Fluid fluid, @Nullable NBTTagCompound tag) {
        // Do a defensive copy of the tag if we're not sure that we can take ownership
        return new AEFluidKey(fluid, tag != null ? tag.copy() : null);
    }

    public static AEFluidKey of(Fluid fluid) {
        return of(fluid, null);
    }

    @Nullable
    public static AEFluidKey of(FluidStack fluidVariant) {
        if (fluidVariant == null || fluidVariant.getFluid() == null) {
            return null;
        }
        return of(fluidVariant.getFluid(), fluidVariant.tag);
    }

    public static boolean matches(AEKey what, FluidStack fluid) {
        return what instanceof AEFluidKey fluidKey && fluidKey.matches(fluid);
    }

    public static boolean is(AEKey what) {
        return what instanceof AEFluidKey;
    }

    public static AEKeyFilter filter() {
        return AEFluidKey::is;
    }

    public boolean matches(FluidStack variant) {
        return variant != null && variant.getFluid() != null && variant.amount > 0 && Objects.equals(tag, variant.tag);
    }

    @Override
    public AEKeyType getType() {
        return AEKeyType.fluids();
    }

    @Override
    public AEFluidKey dropSecondary() {
        return of(fluid, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AEFluidKey aeFluidKey = (AEFluidKey) o;
        // The hash code comparison is a fast-fail for two objects with different NBT or fluid
        return hashCode == aeFluidKey.hashCode && fluid == aeFluidKey.fluid && Objects.equals(tag, aeFluidKey.tag);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public static AEFluidKey fromTag(NBTTagCompound tag) {
        try {
            var fluidId = tag.getString("id");
            if (fluidId.isEmpty()) {
                throw new IllegalArgumentException("Missing fluid id in NBT");
            }

            var fluid = FluidRegistry.getFluid(fluidId);
            if (fluid == null) {
                throw new IllegalArgumentException("Unknown fluid id: " + fluidId);
            }

            NBTTagCompound extraTag = tag.hasKey("tag") ? tag.getCompoundTag("tag") : null;
            return of(fluid, extraTag);
        } catch (Exception e) {
            AELog.debug("Tried to load an invalid fluid key from NBT: %s", tag, e);
            return null;
        }
    }

    @Override
    public NBTTagCompound toTag() {
        NBTTagCompound result = new NBTTagCompound();

        String fluidName = FluidRegistry.getFluidName(fluid);
        if (fluidName == null) {
            fluidName = fluid.getName();
            if (fluidName == null || fluidName.isEmpty()) {
                fluidName = "unknown";
            }
        }

        result.setString("id", fluidName);

        if (tag != null) {
            result.setTag("tag", tag.copy());
        }

        return result;
    }

    @Override
    public Object getPrimaryKey() {
        return fluid;
    }

    @Override
    public String getModId() {
        return FluidRegistry.getModId(new FluidStack(fluid, 1));
    }

    @Override
    public String getDisplayName() {
        return Platform.getFluidDisplayName(this);
    }

    public FluidStack toStack(int amount) {
        return new FluidStack(fluid, amount, tag);
    }

    public Fluid getFluid() {
        return fluid;
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
    public void writeToPacket(PacketBuffer data) {
        String fluidName = FluidRegistry.getFluidName(fluid);
        if (fluidName == null) {
            fluidName = "unknown";
        }
        data.writeString(fluidName);
        data.writeCompoundTag(tag);
    }

    public static AEFluidKey fromPacket(PacketBuffer data) throws IOException {
        String fluidName = data.readString(32767);
        var tag = data.readCompoundTag();
        var fluid = FluidRegistry.getFluid(fluidName);
        if (fluid == null) {
            throw new IllegalArgumentException("Unknown fluid: " + fluidName);
        }

        return new AEFluidKey(fluid, tag);
    }

    public static boolean is(@Nullable GenericStack stack) {
        return stack != null && stack.what() instanceof AEFluidKey;
    }
}
