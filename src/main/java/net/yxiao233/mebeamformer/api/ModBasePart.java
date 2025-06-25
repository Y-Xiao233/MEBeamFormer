package net.yxiao233.mebeamformer.api;

import appeng.api.parts.IPartItem;
import appeng.parts.AEBasePart;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public abstract class ModBasePart extends AEBasePart {
    protected static final int POWERED_FLAG = 1;
    private int clientFlags = 0;

    public ModBasePart(IPartItem<?> partItem) {
        super(partItem);
    }


    @Override
    public void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);

        this.setClientFlags(0);

        if (this.getMainNode().isPowered()) {
            this.setClientFlags(this.getClientFlags() | POWERED_FLAG);
        }
        this.setClientFlags(this.populateFlags(this.getClientFlags()));

        data.writeByte((byte) this.getClientFlags());
    }

    protected int populateFlags(final int cf) {
        return cf;
    }

    @Override
    public boolean readFromStream(FriendlyByteBuf data) {
        final var eh = super.readFromStream(data);

        final var old = this.getClientFlags();
        this.setClientFlags(data.readByte());

        return eh || old != this.getClientFlags();
    }

    @Override
    public boolean isPowered() {
        return (this.getClientFlags() & POWERED_FLAG) == POWERED_FLAG;
    }

    @Override
    public boolean isActive() {
        return this.isPowered();
    }

    public int getClientFlags() {
        return this.clientFlags;
    }

    private void setClientFlags(final int clientFlags) {
        this.clientFlags = clientFlags;
    }
}
