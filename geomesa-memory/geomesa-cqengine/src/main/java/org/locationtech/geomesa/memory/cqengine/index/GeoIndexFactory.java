package org.locationtech.geomesa.memory.cqengine.index;

import com.googlecode.cqengine.attribute.Attribute;
import org.apache.commons.lang3.ArrayUtils;
import org.locationtech.geomesa.memory.cqengine.attribute.GeoIndexType;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeoIndexFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoIndexFactory.class);


    public static <A extends Geometry, O extends SimpleFeature> AbstractGeoIndex<A, O> onAttribute(SimpleFeatureType sft, Attribute<O, A> attribute) {
        int geomAttributeIndex = sft.indexOf(attribute.getAttributeName());
        AttributeDescriptor attributeDescriptor = sft.getDescriptor(geomAttributeIndex);
        GeoIndexType geoIndexType = getGeoIndexType(attributeDescriptor);

        switch (geoIndexType){
            case Bucket:
                return new BucketGeoIndex<A,O>(sft,attribute);
            case STRtree:
                return new STRtreeGeoIndex<A,O>(sft,attribute);
            case QuadTree:
                return new QuadTreeGeoIndex<A,O>(sft,attribute);
        }

        throw new IllegalArgumentException("UNKNOWN GEO INDEX TYPE");

    }

    private static GeoIndexType getGeoIndexType(AttributeDescriptor attributeDescriptor) {
        Object indexType = attributeDescriptor.getUserData().getOrDefault("geo-index-type", GeoIndexType.Bucket.name());
        GeoIndexType geoIndexType = GeoIndexType.Bucket;
        if (indexType instanceof String) {
            if (ArrayUtils.contains(GeoIndexType.values(), indexType)) {

            }
            geoIndexType = GeoIndexType.valueOf((String) indexType);
        } else {
            LOGGER.warn("Unsupported index type: " + indexType + " use default");
        }
        LOGGER.info("Using index type: " + indexType);
        return geoIndexType;
    }
}
