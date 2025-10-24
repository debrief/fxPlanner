package com.planetmayo.usvsim.view.dialogs;

/**
 * Result class for expanding square search configuration
 */
public class ExpandingSquareSearchParams {
    public final double initialDirection;
    public final double legIncrement;
    public final double speed;

    public ExpandingSquareSearchParams(double initialDirection, double legIncrement, double speed) {
        this.initialDirection = initialDirection;
        this.legIncrement = legIncrement;
        this.speed = speed;
    }
}
