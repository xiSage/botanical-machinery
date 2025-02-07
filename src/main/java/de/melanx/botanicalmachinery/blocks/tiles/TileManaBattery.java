package de.melanx.botanicalmachinery.blocks.tiles;

import de.melanx.botanicalmachinery.blocks.BlockManaBattery;
import de.melanx.botanicalmachinery.blocks.base.BotanicalTile;
import de.melanx.botanicalmachinery.config.LibXServerConfig;
import de.melanx.botanicalmachinery.core.TileTags;
import io.github.noeppi_noeppi.libx.inventory.BaseItemStackHandler;
import io.github.noeppi_noeppi.libx.inventory.ItemStackHandlerWrapper;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.common.item.ItemBlackLotus;
import vazkii.botania.common.item.ModItems;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class TileManaBattery extends BotanicalTile {

    public static final ResourceLocation SOLIDIFIED_MANA_ID = new ResourceLocation("mythicbotany", "solidified_mana");

    private static final int MANA_TRANSFER_RATE = 5000;
    private boolean slot1Locked;
    private boolean slot2Locked;

    private final BaseItemStackHandler inventory = new BaseItemStackHandler(2, slot -> {
        this.removePhantomItems();
        this.markDispatchable();
    }, this::isValidStack);

    public TileManaBattery(TileEntityType<?> type) {
        super(type, LibXServerConfig.MaxManaCapacity.manaBattery);
    }

    @Nonnull
    @Override
    public BaseItemStackHandler getInventory() {
        return this.inventory;
    }

    @Override
    public boolean isValidStack(int slot, ItemStack stack) {
        if (stack.getItem() instanceof IManaItem) {
            IManaItem item = (IManaItem) stack.getItem();
            if (slot == 0 && (item.getMana(stack) >= item.getMaxMana(stack) || !item.canReceiveManaFromPool(stack, this) || this.slot1Locked)) return false;
            if (slot == 1 && (item.getMana(stack) <= 0 || !item.canExportManaToPool(stack, this) || this.slot2Locked)) return false;
        } else if (slot == 1 && stack.getItem() instanceof ItemBlackLotus && !this.slot2Locked) {
            return true;
        }

        return stack.getItem() instanceof IManaItem;
    }

    @Override
    public int getCurrentMana() {
        return ((BlockManaBattery) this.getBlockState().getBlock()).variant == BlockManaBattery.Variant.CREATIVE ? this.getManaCap() / 2 : super.getCurrentMana();
    }

    @Override
    public void tick() {
        if (this.world != null && !this.world.isRemote) {
            ItemStack minus = this.inventory.getStackInSlot(0);
            ItemStack plus = this.inventory.getStackInSlot(1);
            if (!minus.isEmpty()) {
                if (minus.getItem() instanceof IManaItem) {
                    IManaItem manaItem = (IManaItem) minus.getItem();
                    if (manaItem.canReceiveManaFromPool(minus, this)) {
                        int maxManaValue = ((BlockManaBattery) this.getBlockState().getBlock()).variant == BlockManaBattery.Variant.NORMAL ? MANA_TRANSFER_RATE : Integer.MAX_VALUE;
                        int manaValue = Math.min(maxManaValue, Math.min(this.getCurrentMana(), manaItem.getMaxMana(minus) - manaItem.getMana(minus)));
                        manaItem.addMana(minus, manaValue);
                        this.receiveMana(-manaValue);
                        this.markDirty();
                        this.markDispatchable();
                        this.removePhantomItems();
                    }
                }
            }
            if (!plus.isEmpty()) {
                if (plus.getItem() instanceof IManaItem) {
                    IManaItem manaItem = (IManaItem) plus.getItem();
                    if (manaItem.canExportManaToPool(plus, this)) {
                        int maxManaValue = ((BlockManaBattery) this.getBlockState().getBlock()).variant == BlockManaBattery.Variant.NORMAL ? MANA_TRANSFER_RATE : Integer.MAX_VALUE;
                        int manaValue = Math.min(maxManaValue, Math.min(this.getManaCap() - this.getCurrentMana(), manaItem.getMana(plus)));
                        manaItem.addMana(plus, -manaValue);
                        this.receiveMana(manaValue);
                        this.markDirty();
                        this.markDispatchable();
                        this.removePhantomItems();
                    }
                } else if (plus.getItem() instanceof ItemBlackLotus) {
                    ItemBlackLotus item = (ItemBlackLotus) plus.getItem();
                    this.receiveMana(item == ModItems.blackerLotus ? 100000 : 8000);
                    this.inventory.setStackInSlot(1, ItemStack.EMPTY);
                    this.markDirty();
                    this.markDispatchable();
                }
            }
            for (Direction direction : Direction.values()) {
                TileEntity tile = this.world.getTileEntity(this.getPos().offset(direction));
                if (tile instanceof BotanicalTile) {
                    BotanicalTile offsetTile = (BotanicalTile) tile;
                    if (!offsetTile.isFull()) {
                        int maxManaValue = ((BlockManaBattery) this.getBlockState().getBlock()).variant == BlockManaBattery.Variant.NORMAL ? MANA_TRANSFER_RATE : Integer.MAX_VALUE;
                        int manaValue = Math.min(maxManaValue, Math.min(this.getCurrentMana(), offsetTile.getManaCap() - offsetTile.getCurrentMana()));
                        if (manaValue <= 0 && offsetTile instanceof TileMechanicalManaPool)
                            manaValue = Math.min(this.getCurrentMana(), MANA_TRANSFER_RATE);
                        this.receiveMana(-manaValue);
                        offsetTile.receiveMana(manaValue);
                        this.markDirty();
                        this.markDispatchable();
                    }
                }
            }
        }
    }

    public boolean isSlot1Locked() {
        return this.slot1Locked;
    }

    public boolean isSlot2Locked() {
        return this.slot2Locked;
    }

    public void setSlot1Locked(boolean slot1Locked) {
        // Do not remove the condition! (Because of packets)
        if (slot1Locked != this.slot1Locked) {
            this.slot1Locked = slot1Locked;
            this.markDirty();
        }
    }

    public void setSlot2Locked(boolean slot2Locked) {
        // Do not remove the condition! (Because of packets)
        if (slot2Locked != this.slot2Locked) {
            this.slot2Locked = slot2Locked;
            this.markDirty();
        }
    }

    @Override
    public int getComparatorOutput() {
        return (int) Math.round(this.getCurrentMana() / (double) this.getManaCap() * 15d);
    }

    @Override
    protected LazyOptional<IItemHandlerModifiable> createCap(Supplier<IItemHandlerModifiable> inventory) {
        return ItemStackHandlerWrapper.createLazy(inventory, slot -> {
            ItemStack minus = inventory.get().getStackInSlot(0);
            ItemStack plus = inventory.get().getStackInSlot(1);
            if (slot == 0 && minus.getItem() instanceof IManaItem) {
                IManaItem manaItem = (IManaItem) minus.getItem();
                return manaItem.getMana(minus) >= manaItem.getMaxMana(minus);
            } else if (slot == 1 && plus.getItem() instanceof IManaItem) {
                IManaItem manaItem = (IManaItem) plus.getItem();
                return manaItem.getMana(plus) <= 0 && !SOLIDIFIED_MANA_ID.equals(plus.getItem().getRegistryName());
            }
            return true;
        }, null);
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT cmp) {
        super.read(state, cmp);
        this.slot1Locked = cmp.getBoolean(TileTags.SLOT_1_LOCKED);
        this.slot2Locked = cmp.getBoolean(TileTags.SLOT_2_LOCKED);
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT cmp) {
        cmp.putBoolean(TileTags.SLOT_1_LOCKED, this.slot1Locked);
        cmp.putBoolean(TileTags.SLOT_2_LOCKED, this.slot2Locked);
        return super.write(cmp);
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT cmp) {
        if (this.world != null && !this.world.isRemote) return;
        super.handleUpdateTag(state, cmp);
        this.slot1Locked = cmp.getBoolean(TileTags.SLOT_1_LOCKED);
        this.slot2Locked = cmp.getBoolean(TileTags.SLOT_2_LOCKED);
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        if (this.world != null && this.world.isRemote) return super.getUpdateTag();
        CompoundNBT cmp = super.getUpdateTag();
        cmp.putBoolean(TileTags.SLOT_1_LOCKED, this.slot1Locked);
        cmp.putBoolean(TileTags.SLOT_2_LOCKED, this.slot2Locked);
        return cmp;
    }

    private void removePhantomItems() {
        for (int slot = 0; slot < this.inventory.getSlots(); slot++) {
            ItemStack stack = this.inventory.getStackInSlot(slot);
            if (SOLIDIFIED_MANA_ID.equals(stack.getItem().getRegistryName()) && stack.getItem() instanceof IManaItem && ((IManaItem) stack.getItem()).getMana(stack) <= 0) {
                this.inventory.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }
}
