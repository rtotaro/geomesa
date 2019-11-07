package org.locationtech.geomesa.memory.cqengine.attribute;

public class STRtreeIndexParam implements GeoIndexParams {

    int nodeCapacity = 10;

    public STRtreeIndexParam() {
    }

    public STRtreeIndexParam(int nodeCapacity) {
        this.nodeCapacity = nodeCapacity;
    }

    public int getNodeCapacity() {
        return nodeCapacity;
    }

}
