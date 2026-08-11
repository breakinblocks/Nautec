package com.breakinblocks.nautec.content.items.submarine;

import com.breakinblocks.nautec.NTConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum SubmarineModuleType implements StringRepresentable {
    SOLAR("solar", true),
    BOOSTER("booster", false),
    STEALTH("stealth", false),
    ARMOR("armor", true),
    SONAR("sonar", false),
    SHIELD("shield", false),
    IMPULSE_LASER("impulse_laser", false),
    TELEPORT("teleport", false);

    private final String name;
    private final boolean passive;

    SubmarineModuleType(String name, boolean passive) {
        this.name = name;
        this.passive = passive;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public String itemId() {
        return this.name + "_module";
    }

    public boolean isPassive() {
        return this.passive;
    }

    public String translationKey() {
        return "nautec.submarine.module." + this.name;
    }

    public Component displayName() {
        return Component.translatable(translationKey());
    }

    public int powerCost() {
        return switch (this) {
            case BOOSTER -> NTConfig.submarineBoostPowerCost;
            case STEALTH -> NTConfig.submarineStealthPowerCost;
            case SONAR -> NTConfig.submarineSonarPowerCost;
            case SHIELD -> NTConfig.submarineShieldPowerCost;
            case IMPULSE_LASER -> NTConfig.submarineLaserPowerCost;
            case TELEPORT -> NTConfig.submarineTeleportPowerCost;
            case SOLAR, ARMOR -> 0;
        };
    }

    public int activeTicks() {
        return switch (this) {
            case BOOSTER -> NTConfig.submarineBoostDurationTicks;
            case STEALTH -> NTConfig.submarineStealthDurationTicks;
            default -> 0;
        };
    }

    public int cooldownTicks() {
        return switch (this) {
            case BOOSTER -> NTConfig.submarineBoostDurationTicks + NTConfig.submarineBoostCooldownTicks;
            case STEALTH -> NTConfig.submarineStealthDurationTicks + NTConfig.submarineStealthCooldownTicks;
            case SONAR -> NTConfig.submarineSonarCooldownTicks;
            case SHIELD -> NTConfig.submarineShieldCooldownTicks;
            case TELEPORT -> NTConfig.submarineTeleportCooldownTicks;
            case IMPULSE_LASER, SOLAR, ARMOR -> 0;
        };
    }
}
