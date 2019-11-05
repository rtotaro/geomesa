package org.locationtech.geomesa.memory.cqengine.attribute;

import org.locationtech.geomesa.memory.cqengine.index.BucketGeoIndex;

public enum GeoIndexType {

    STRtree(new String[]{"nodeCapacity"}),QuadTree(new String[]{}),Bucket(new String[]{BucketGeoIndex.X_BUCKET,BucketGeoIndex.X_BUCKET});

    private String[] allowedParameters;

    GeoIndexType(String[] allowedParameters) {
        this.allowedParameters = allowedParameters;
    }

    public String[] getAllowedParameters() {
        return allowedParameters;
    }
}
