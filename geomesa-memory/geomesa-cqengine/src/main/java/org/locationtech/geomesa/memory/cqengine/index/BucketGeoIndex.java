package org.locationtech.geomesa.memory.cqengine.index;

import com.googlecode.cqengine.attribute.Attribute;
import org.locationtech.geomesa.memory.cqengine.attribute.BucketIndexParam;
import org.locationtech.geomesa.memory.cqengine.attribute.GeoIndexParams;
import org.locationtech.geomesa.utils.index.BucketIndex;
import org.locationtech.geomesa.utils.index.SizeSeparatedBucketIndex;
import org.locationtech.geomesa.utils.index.SizeSeparatedBucketIndex$;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.text.MessageFormat;

public class BucketGeoIndex<A extends Geometry, O extends SimpleFeature> extends AbstractGeoIndex<A, O> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BucketGeoIndex.class);

    public BucketGeoIndex(SimpleFeatureType sft, Attribute<O, A> attribute, Option<GeoIndexParams> geoIndexParams) {
        super(sft, attribute);
        geomAttributeIndex = sft.indexOf(attribute.getAttributeName());
        AttributeDescriptor attributeDescriptor = sft.getDescriptor(geomAttributeIndex);

        BucketIndexParam params = geoIndexParams.getOrElse(BucketIndexParam::new);

        int xBuckets = params.getxBuckets();
        int yBuckets = params.getyBuckets();
        LOGGER.debug(MessageFormat.format("Bucket Index in use :xBucket = {0}, yBucket={1}", xBuckets, yBuckets));

        if (attributeDescriptor.getType().getBinding() == Point.class) {
            index = new BucketIndex<>(xBuckets, params.getyBuckets(), new Envelope(-180.0, 180.0, -90.0, 90.0));
        } else {
            index = new SizeSeparatedBucketIndex<>(SizeSeparatedBucketIndex$.MODULE$.DefaultTiers(),
                    xBuckets / 360d,
                    yBuckets / 180d,
                    new Envelope(-180.0, 180.0, -90.0, 90.0));
        }
    }


}
