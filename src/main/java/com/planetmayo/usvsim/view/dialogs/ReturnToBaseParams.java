package com.planetmayo.usvsim.view.dialogs;

import com.planetmayo.usvsim.model.geometry.Position;

/**
 * Result class for return-to-base configuration
 */
public class ReturnToBaseParams {
    public final Position baseLocation;
    public final double speed;

    public ReturnToBaseParams(Position baseLocation, double speed) {
        this.baseLocation = baseLocation;
        this.speed = speed;
    }
}
