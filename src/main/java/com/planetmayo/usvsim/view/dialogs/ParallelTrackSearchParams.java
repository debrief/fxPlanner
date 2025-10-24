package com.planetmayo.usvsim.view.dialogs;

/**
 * Result class for ParallelTrackSearchDialog output.
 */
public class ParallelTrackSearchParams {
    public final double orientation;
    public final double spacing;
    public final double speed;

    public ParallelTrackSearchParams(double orientation, double spacing, double speed) {
        this.orientation = orientation;
        this.spacing = spacing;
        this.speed = speed;
    }
}
