package org.locationtech.geomesa.memory.cqengine.index;

import com.googlecode.cqengine.attribute.Attribute;
import org.locationtech.geomesa.utils.index.BucketIndex;
import org.locationtech.geomesa.utils.index.SizeSeparatedBucketIndex;
import org.locationtech.geomesa.utils.index.SizeSeparatedBucketIndex$;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

@Deprecated
public class GeoIndex<A extends Geometry, O extends SimpleFeature>  extends AbstractGeoIndex<A,O> {
    public GeoIndex(SimpleFeatureType sft, Attribute<O, A> attribute) {
        super(sft, attribute);
    }

    @Deprecated
    public GeoIndex(SimpleFeatureType sft, Attribute<O, A> attribute, int xBuckets, int yBuckets) {
        super(sft,attribute);
        geomAttributeIndex = sft.indexOf(attribute.getAttributeName());
        AttributeDescriptor attributeDescriptor = sft.getDescriptor(geomAttributeIndex);
        if (attributeDescriptor.getType().getBinding() == Point.class) {
            index = new BucketIndex<>(xBuckets, yBuckets, new Envelope(-180.0, 180.0, -90.0, 90.0));
        } else {
            index = new SizeSeparatedBucketIndex<>(SizeSeparatedBucketIndex$.MODULE$.DefaultTiers(),
                    xBuckets / 360d,
                    yBuckets / 180d,
                    new Envelope(-180.0, 180.0, -90.0, 90.0));
        }
    }

    @Deprecated
    public static <A extends Geometry, O extends SimpleFeature> GeoIndex<A, O> onAttribute(SimpleFeatureType sft, Attribute<O, A> attribute) {
        return (GeoIndex<A, O>)onAttribute(sft, attribute, 360, 180);
    }

    @Deprecated
    public static <A extends Geometry, O extends SimpleFeature> GeoIndex<A, O> onAttribute(SimpleFeatureType sft, Attribute<O, A> attribute, int xBuckets, int yBuckets) {
        return new GeoIndex<A, O>(sft, attribute, xBuckets, yBuckets);
    }


}
