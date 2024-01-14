package com.huandoriti.paint.game.canvastransfer;

import com.huandoriti.paint.game.canvastransfer.Forma;

public class Rect extends Forma {
    public Rect(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    @Override
    public String toString() {
        return "Rect{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
