package com.condor.nexussoft.timeclock.attendance.infrastructure.persistence;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

final class GeoSupport {

    private static final GeometryFactory FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private GeoSupport() {
    }

    static Point point(double latitude, double longitude) {
        return FACTORY.createPoint(new Coordinate(longitude, latitude));
    }
}
