package com.planetmayo.usvsim.model.platform;

/**
 * Control demands from behaviour to platform.
 */
public final class PlatformDemand {
    private final double demandedHeading;
    private final double demandedSpeed;
    private final double demandedDepth;
    private final TurnDirection turnDirection;

    public PlatformDemand(double demandedHeading, double demandedSpeed, double demandedDepth, TurnDirection turnDirection) {
        this.demandedHeading = demandedHeading;
        this.demandedSpeed = demandedSpeed;
        this.demandedDepth = demandedDepth;
        this.turnDirection = turnDirection;
    }

    public double getDemandedHeading() { return demandedHeading; }
    public double getDemandedSpeed() { return demandedSpeed; }
    public double getDemandedDepth() { return demandedDepth; }
    public TurnDirection getTurnDirection() { return turnDirection; }
}
