package com.breakinblocks.nautec.api.client.renderer.robotArms;

import com.breakinblocks.nautec.api.client.renderer.blockentities.BERenderState;
import com.breakinblocks.nautec.client.renderer.blockentities.ItemStackRenderState;
import com.breakinblocks.nautec.content.items.RobotArmItem;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public class RobotArmRenderState extends BERenderState {
    @Nullable
    public RobotArmItem armItem;
    public Direction facing = Direction.NORTH;
    public float partialTick;
    public float middleAngle;
    public float prevMiddleAngle;
    public float tipAngle;
    public float prevTipAngle;
    public int lightAbove;
    public final ItemStackRenderState heldItem = new ItemStackRenderState();
}
