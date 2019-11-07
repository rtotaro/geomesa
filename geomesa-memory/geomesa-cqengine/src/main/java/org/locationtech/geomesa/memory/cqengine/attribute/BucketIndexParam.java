package org.locationtech.geomesa.memory.cqengine.attribute;

public class BucketIndexParam implements GeoIndexParams {
    private int xBuckets = 360;
    private int yBuckets = 180;


    public BucketIndexParam() {
    }

    public BucketIndexParam(int xBuckets, int yBuckets) {
        this.xBuckets = xBuckets;
        this.yBuckets = yBuckets;
    }

    public int getxBuckets() {
        return xBuckets;
    }

    public int getyBuckets() {
        return yBuckets;
    }
}
