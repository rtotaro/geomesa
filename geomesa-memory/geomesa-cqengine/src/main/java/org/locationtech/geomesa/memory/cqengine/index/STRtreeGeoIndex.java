package org.locationtech.geomesa.memory.cqengine.index;

import com.googlecode.cqengine.attribute.Attribute;
import org.locationtech.geomesa.utils.index.WrappedSTRtree;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

public class STRtreeGeoIndex<A extends Geometry, O extends SimpleFeature> extends AbstractGeoIndex<A, O> {

    private static final Logger LOGGER = LoggerFactory.getLogger(STRtreeGeoIndex.class);

    public static String NODE_CAPACITY = "nodeCapacity";

    public STRtreeGeoIndex(SimpleFeatureType sft, Attribute<O, A> attribute) {
        super(sft, attribute);

        geomAttributeIndex = sft.indexOf(attribute.getAttributeName());
        AttributeDescriptor attributeDescriptor = sft.getDescriptor(geomAttributeIndex);

        Integer nodeCapacity = (Integer) attributeDescriptor.getUserData().getOrDefault(NODE_CAPACITY,null);

        LOGGER.debug(MessageFormat.format("STR Tree Index in use :nodeCapacity = {0}", nodeCapacity));

        index = nodeCapacity != null ? new WrappedSTRtree<>(nodeCapacity) : new WrappedSTRtree<>();
    }
}
