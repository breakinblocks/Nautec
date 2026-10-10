package com.breakinblocks.nautec.utils;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class ARGB {
    private static final short[] SRGB_TO_LINEAR = new short[256];
    private static final byte[] LINEAR_TO_SRGB = new byte[1024];

    static {
        for (int i = 0; i < SRGB_TO_LINEAR.length; i++) {
            float channel = i / 255.0F;
            float linear = channel >= 0.04045F ? (float) Math.pow((channel + 0.055) / 1.055, 2.4) : channel / 12.92F;
            SRGB_TO_LINEAR[i] = (short) Math.round(linear * 1023.0F);
        }
        for (int i = 0; i < LINEAR_TO_SRGB.length; i++) {
            float channel = i / 1023.0F;
            float srgb = channel >= 0.0031308F ? (float) (1.055 * Math.pow(channel, 1.0 / 2.4) - 0.055) : 12.92F * channel;
            LINEAR_TO_SRGB[i] = (byte) Math.round(srgb * 255.0F);
        }
    }

    private ARGB() {
    }

    public static int alpha(int color) {
        return color >>> 24;
    }

    public static int red(int color) {
        return color >> 16 & 0xFF;
    }

    public static int green(int color) {
        return color >> 8 & 0xFF;
    }

    public static int blue(int color) {
        return color & 0xFF;
    }

    public static int color(int alpha, int red, int green, int blue) {
        return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
    }

    public static int color(int red, int green, int blue) {
        return color(255, red, green, blue);
    }

    public static int color(Vec3 vec) {
        return color(as8BitChannel((float) vec.x()), as8BitChannel((float) vec.y()), as8BitChannel((float) vec.z()));
    }

    public static int color(int alpha, int rgb) {
        return alpha << 24 | rgb & 0xFFFFFF;
    }

    public static int color(float alpha, int rgb) {
        return as8BitChannel(alpha) << 24 | rgb & 0xFFFFFF;
    }

    public static int colorFromFloat(float alpha, float red, float green, float blue) {
        return color(as8BitChannel(alpha), as8BitChannel(red), as8BitChannel(green), as8BitChannel(blue));
    }

    public static int white(float alpha) {
        return as8BitChannel(alpha) << 24 | 0xFFFFFF;
    }

    public static int multiply(int lhs, int rhs) {
        if (lhs == -1) {
            return rhs;
        }
        if (rhs == -1) {
            return lhs;
        }
        return color(alpha(lhs) * alpha(rhs) / 255, red(lhs) * red(rhs) / 255, green(lhs) * green(rhs) / 255, blue(lhs) * blue(rhs) / 255);
    }

    public static int multiplyAlpha(int color, float alphaMultiplier) {
        if (color == 0 || alphaMultiplier <= 0.0F) {
            return 0;
        }
        return alphaMultiplier >= 1.0F ? color : color(alphaFloat(color) * alphaMultiplier, color);
    }

    public static int scaleRGB(int color, float scale) {
        return scaleRGB(color, scale, scale, scale);
    }

    public static int scaleRGB(int color, float scaleR, float scaleG, float scaleB) {
        return color(alpha(color),
                Mth.clamp((int) (red(color) * scaleR), 0, 255),
                Mth.clamp((int) (green(color) * scaleG), 0, 255),
                Mth.clamp((int) (blue(color) * scaleB), 0, 255));
    }

    public static int scaleRGB(int color, int scale) {
        return color(alpha(color),
                (int) Mth.clamp((long) red(color) * scale / 255L, 0, 255),
                (int) Mth.clamp((long) green(color) * scale / 255L, 0, 255),
                (int) Mth.clamp((long) blue(color) * scale / 255L, 0, 255));
    }

    public static int greyscale(int color) {
        int grey = (int) (red(color) * 0.3F + green(color) * 0.59F + blue(color) * 0.11F);
        return color(alpha(color), grey, grey, grey);
    }

    public static int srgbLerp(float alpha, int p0, int p1) {
        return color(
                Mth.lerpInt(alpha, alpha(p0), alpha(p1)),
                Mth.lerpInt(alpha, red(p0), red(p1)),
                Mth.lerpInt(alpha, green(p0), green(p1)),
                Mth.lerpInt(alpha, blue(p0), blue(p1)));
    }

    public static int linearLerp(float alpha, int p0, int p1) {
        return color(
                Mth.lerpInt(alpha, alpha(p0), alpha(p1)),
                LINEAR_TO_SRGB[Mth.lerpInt(alpha, SRGB_TO_LINEAR[red(p0)], SRGB_TO_LINEAR[red(p1)])] & 0xFF,
                LINEAR_TO_SRGB[Mth.lerpInt(alpha, SRGB_TO_LINEAR[green(p0)], SRGB_TO_LINEAR[green(p1)])] & 0xFF,
                LINEAR_TO_SRGB[Mth.lerpInt(alpha, SRGB_TO_LINEAR[blue(p0)], SRGB_TO_LINEAR[blue(p1)])] & 0xFF);
    }

    public static int opaque(int color) {
        return color | 0xFF000000;
    }

    public static int transparent(int color) {
        return color & 0xFFFFFF;
    }

    public static int as8BitChannel(float value) {
        return Mth.floor(value * 255.0F);
    }

    public static float alphaFloat(int color) {
        return alpha(color) / 255.0F;
    }

    public static float redFloat(int color) {
        return red(color) / 255.0F;
    }

    public static float greenFloat(int color) {
        return green(color) / 255.0F;
    }

    public static float blueFloat(int color) {
        return blue(color) / 255.0F;
    }

    public static int toABGR(int color) {
        return color & 0xFF00FF00 | (color & 0xFF0000) >> 16 | (color & 0xFF) << 16;
    }

    public static int average(int lhs, int rhs) {
        return color((alpha(lhs) + alpha(rhs)) / 2, (red(lhs) + red(rhs)) / 2, (green(lhs) + green(rhs)) / 2, (blue(lhs) + blue(rhs)) / 2);
    }
}
