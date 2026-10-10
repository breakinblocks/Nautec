package com.breakinblocks.nautec.content.entities.submarine;

public record SubmarineInput(boolean forward, boolean backward, boolean left, boolean right, boolean jump, boolean shift, boolean sprint) {
    public static final SubmarineInput EMPTY = new SubmarineInput(false, false, false, false, false, false, false);
}
