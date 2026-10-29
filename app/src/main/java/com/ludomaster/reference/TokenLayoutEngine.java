package com.ludomaster.reference;

/** Pure presentation geometry for tokens; it does not decide legal moves. */
final class TokenLayoutEngine {
    private static final float[] STACK_SCALE = {0f, 1f, 0.74f, 0.61f, 0.51f};

    private TokenLayoutEngine() {}

    /** Keep the painted token within one board cell while retaining its aspect ratio. */
    static float boardScale(float cellSizePx, float nominalTokenWidthPx, float nominalTokenHeightPx) {
        if (cellSizePx <= 0f || nominalTokenWidthPx <= 0f || nominalTokenHeightPx <= 0f) return 1f;
        float fitWidth = (cellSizePx * 0.88f) / nominalTokenWidthPx;
        float fitHeight = (cellSizePx * 0.92f) / nominalTokenHeightPx;
        return Math.max(0.35f, Math.min(1.25f,Math.min(fitWidth,fitHeight)));
    }

    /** Relative scale for a legal group sharing one square. */
    static float stackScale(int count) {
        if (count <= 0) return 0f;
        return count < STACK_SCALE.length ? STACK_SCALE[count] : 0.42f;
    }

    /** Offset from the single-token center, expressed in pixels. */
    static Offset offset(int index, int count, boolean horizontal, float cellSizePx) {
        if (count <= 1 || index < 0 || index >= count || cellSizePx <= 0f)
            return new Offset(0f, 0f);

        float x;
        float y;
        if (count == 2) {
            x = index == 0 ? -0.19f : 0.19f;
            y = 0f;
        } else if (count == 3) {
            float[][] points = {{-0.19f, -0.11f}, {0.19f, -0.11f}, {0f, 0.22f}};
            x = points[index][0];
            y = points[index][1];
        } else if (count == 4) {
            float[][] points = {{-0.22f, -0.22f}, {0.22f, -0.22f},
                    {-0.22f, 0.22f}, {0.22f, 0.22f}};
            x = points[index][0];
            y = points[index][1];
        } else {
            int columns = 4;
            int row = index / columns;
            int column = index % columns;
            float columnStep = 0.19f;
            float rowStep = 0.19f;
            int rows = (count + columns - 1) / columns;
            int itemsInRow = Math.min(columns,count-row*columns);
            x = (column - (itemsInRow - 1) / 2f) * columnStep;
            y = (row - (rows - 1) / 2f) * rowStep;
        }

        if (!horizontal) {
            float swap = x;
            x = y;
            y = swap;
        }
        return new Offset(x * cellSizePx, y * cellSizePx);
    }

    static final class Offset {
        final float x;
        final float y;

        Offset(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
