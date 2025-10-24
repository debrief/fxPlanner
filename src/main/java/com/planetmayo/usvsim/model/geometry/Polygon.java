package com.planetmayo.usvsim.model.geometry;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Search area boundary polygon with JTS integration for spatial operations.
 */
public final class Polygon {
    private final List<Position> vertices;
    private final org.locationtech.jts.geom.Polygon jtsPolygon;

    /**
     * Creates a new Polygon.
     *
     * @param vertices ordered vertices (minimum 3)
     * @throws IllegalArgumentException if < 3 vertices or self-intersecting
     */
    public Polygon(List<Position> vertices) {
        if (vertices.size() < 3) {
            throw new IllegalArgumentException("Polygon requires minimum 3 vertices");
        }
        this.vertices = new ArrayList<>(vertices);
        this.jtsPolygon = createJTSPolygon();
        if (!jtsPolygon.isValid()) {
            throw new IllegalArgumentException("Polygon is self-intersecting or invalid");
        }
    }

    private org.locationtech.jts.geom.Polygon createJTSPolygon() {
        GeometryFactory gf = new GeometryFactory();
        Coordinate[] coords = new Coordinate[vertices.size() + 1];
        for (int i = 0; i < vertices.size(); i++) {
            coords[i] = new Coordinate(vertices.get(i).getLongitude(), vertices.get(i).getLatitude());
        }
        coords[vertices.size()] = coords[0]; // Close ring
        LinearRing ring = gf.createLinearRing(coords);
        return gf.createPolygon(ring);
    }

    public List<Position> getVertices() {
        return new ArrayList<>(vertices);
    }

    public org.locationtech.jts.geom.Polygon getJTSPolygon() {
        return jtsPolygon;
    }

    public boolean contains(Position p) {
        org.locationtech.jts.geom.Point pt = new GeometryFactory()
            .createPoint(new Coordinate(p.getLongitude(), p.getLatitude()));
        return jtsPolygon.contains(pt);
    }

    public Position centroid() {
        org.locationtech.jts.geom.Point centroid = jtsPolygon.getCentroid();
        return Position.of(centroid.getY(), centroid.getX());
    }

    public boolean isValid() {
        return jtsPolygon.isValid();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Polygon)) return false;
        Polygon polygon = (Polygon) o;
        return Objects.equals(vertices, polygon.vertices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vertices);
    }

    @Override
    public String toString() {
        return String.format("Polygon(%d vertices)", vertices.size());
    }
}
