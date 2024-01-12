package com.huandoriti.paint.game;

import javafx.scene.paint.Color;

public class Oval extends Forma{

    public String color;

    public Oval(double x, double y, double width, double height, String color) {
        super(x, y, width, height);
        this.color = color;
    }

    @Override
    public String toString() {
        return "Oval{" +
                "color='" + color + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
