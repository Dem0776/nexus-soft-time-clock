package com.condor.nexussoft.timeclock.geofencing.infrastructure.persistence;

import com.condor.nexussoft.timeclock.geofencing.domain.GeoPoint;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

final class GeoSupport {

    private static final GeometryFactory FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private GeoSupport() {
    }

    static Point toPoint(GeoPoint geo) {
        return FACTORY.createPoint(new Coordinate(geo.longitude(), geo.latitude()));
    }

    static GeoPoint toGeoPoint(Point point) {
        return new GeoPoint(point.getY(), point.getX());
    }
}
