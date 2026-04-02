package dev.wyfy.shulkervault.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

public sealed interface VaultLocation permits VaultLocation.InBlock, VaultLocation.InItem {

    void write(FriendlyByteBuf buf);

    static VaultLocation read(FriendlyByteBuf buf) {
        if (buf == null) {
            return new InBlock(BlockPos.ZERO);
        }
        byte type = buf.readByte();
        return switch (type) {
            case 0 -> new InBlock(buf.readBlockPos());
            case 1 -> new InItem(buf.readEnum(InteractionHand.class));
            default -> throw new IllegalArgumentException("Unknown VaultLocation type: " + type);
        };
    }

    record InBlock(BlockPos pos) implements VaultLocation {
        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeByte(0);
            buf.writeBlockPos(pos);
        }
    }

    record InItem(InteractionHand hand) implements VaultLocation {
        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeByte(1);
            buf.writeEnum(hand);
        }
    }
}
