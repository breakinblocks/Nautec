package com.breakinblocks.nautec.content.items;


import java.util.List;
import com.breakinblocks.nautec.registries.NTArmorMaterials;
import com.breakinblocks.nautec.data.NTDataComponentsUtils;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.utils.Tooltips;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class DivingSuitArmorItem extends ArmorItem {

    public DivingSuitArmorItem(ArmorItem.Type type, Properties properties) {
        super(NTArmorMaterials.DIVING_SUIT, type, properties.stacksTo(1).durability(type.getDurability(NTArmorMaterials.DIVING_SUIT_DURABILITY)));
    }

    @Override
    public @Nullable ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        if (this.getType() == ArmorItem.Type.HELMET) {
            return NTArmorMaterials.DIVING_SUIT_HELMET_TEXTURE;
        }
        return super.getArmorTexture(stack, entity, slot, layer, innerModel);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide()) {
            return;
        }
        if (entity instanceof Player player) {
            if (hasFullArmorSet(stack, player)) {
                if (player.isUnderWater() && !player.isCreative() && !player.isSpectator()) {
                    if (level.getGameTime() % 20 == 0) {
                        int currentOxygen = NTDataComponentsUtils.getOxygenLevels(stack);
                        if (currentOxygen > 0) {
                            NTDataComponentsUtils.setOxygenLevels(stack, currentOxygen - 1);
                            player.setAirSupply(player.getMaxAirSupply());
                        }
                    }
                }
            }
        }
    }

    private static boolean hasFullArmorSet(ItemStack stack, Player player) {
        return stack == player.getItemBySlot(EquipmentSlot.CHEST) && isWearingFullSuit(player);
    }

    public static boolean isWearingFullSuit(Player player) {
        return player.getItemBySlot(EquipmentSlot.CHEST).is(NTItems.DIVING_CHESTPLATE) &&
                player.getItemBySlot(EquipmentSlot.HEAD).is(NTItems.DIVING_HELMET) &&
                player.getItemBySlot(EquipmentSlot.LEGS).is(NTItems.DIVING_LEGGINGS) &&
                player.getItemBySlot(EquipmentSlot.FEET).is(NTItems.DIVING_BOOTS);
    }

    public static float withoutWaterPenalties(Player player, float speed) {
        if (!player.isInWater() || !isWearingFullSuit(player)) {
            return speed;
        }
        if (player.isEyeInFluid(FluidTags.WATER)) {
            float submerged = (float) player.getAttributeValue(Attributes.SUBMERGED_MINING_SPEED);
            if (submerged > 0 && submerged < 1) {
                speed /= submerged;
            }
        }
        return player.onGround() ? speed : speed * 5.0F;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (stack.is(NTItems.DIVING_HELMET.get())) {
            Tooltips.trans(tooltipComponents, "nautec.helm.desc", ChatFormatting.GRAY);
        }
        Tooltips.trans(tooltipComponents, "nautec.diving_suit.mining", ChatFormatting.GRAY);

        if (stack.is(NTItems.DIVING_CHESTPLATE.get())) {
            int oxygen = NTDataComponentsUtils.getOxygenLevels(stack);
            int minutesRemaining = oxygen / 60;
            int secondsRemaining = oxygen % 60;

            int red = (int) (255 * (1 - (oxygen / 600.0)));
            int green = (int) (255 * (oxygen / 600.0));

            int colorHex = (red << 16) | (green << 8);

            tooltipComponents.add(Component.translatable("nautec.diving_suit.oxygen", minutesRemaining, secondsRemaining)
                    .withStyle(style -> style.withColor(TextColor.fromRgb(colorHex))));
            tooltipComponents.add(Component.translatable("nautec.diving_suit.refill").withStyle(ChatFormatting.GRAY));
        }
    }
}
