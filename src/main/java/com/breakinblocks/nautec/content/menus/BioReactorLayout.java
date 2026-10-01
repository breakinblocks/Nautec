package com.breakinblocks.nautec.content.menus;

public record BioReactorLayout(int imageWidth, int imageHeight, int inventoryY,
                               int[][] colonies, int[][] outputs, int[][] nutrients, int[][] upgrades,
                               int[][] vitalityBars, int[][] progressArrows, int[][] progressBars,
                               int[] summaryArrow) {
    public static final int ARROW_WIDTH = 24;
    public static final int ARROW_HEIGHT = 10;

    public static final BioReactorLayout BIO_REACTOR = new BioReactorLayout(176, 174, 92,
            new int[][]{{52, 11}, {52, 33}, {52, 55}},
            new int[][]{{107, 12}, {107, 34}, {107, 56}},
            new int[][]{{20, 23}, {20, 45}},
            new int[][]{{148, 23}, {148, 45}},
            new int[][]{{47, 12, 3, 16}, {47, 34, 3, 16}, {47, 56, 3, 16}},
            new int[][]{{76, 16}, {76, 38}, {76, 60}},
            null,
            null);

    public static final BioReactorLayout INDUSTRIAL = industrial();

    private static BioReactorLayout industrial() {
        int[][] colonies = new int[9][];
        int[][] outputs = new int[9][];
        int[][] vitality = new int[9][];
        int[][] bars = new int[9][];
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                int index = row * 3 + column;
                int x = 12 + column * 24;
                int y = 16 + row * 24;
                colonies[index] = new int[]{x, y};
                vitality[index] = new int[]{x + 19, y + 1, 3, 16};
                bars[index] = new int[]{x + 1, y + 19, 16, 2};
                outputs[index] = new int[]{116 + column * 18, 22 + row * 18};
            }
        }
        int[][] nutrients = new int[3][];
        for (int i = 0; i < 3; i++) {
            nutrients[i] = new int[]{12 + i * 18, 96};
        }
        int[][] upgrades = new int[4][];
        for (int i = 0; i < 4; i++) {
            upgrades[i] = new int[]{98 + i * 18, 96};
        }
        return new BioReactorLayout(176, 208, 126, colonies, outputs, nutrients, upgrades, vitality, null, bars, new int[]{86, 45});
    }
}
