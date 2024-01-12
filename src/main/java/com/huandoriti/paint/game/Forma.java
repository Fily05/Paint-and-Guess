package com.huandoriti.paint.game;

import java.io.Serializable;

public class Forma implements Serializable {
    public double x, y, width, height;

    public Forma(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return "Forma{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
