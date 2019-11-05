package org.locationtech.geomesa.memory.cqengine.index;

import com.googlecode.cqengine.attribute.Attribute;
import org.locationtech.geomesa.utils.index.WrappedQuadtree;
import org.locationtech.geomesa.utils.index.WrappedSTRtree;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import java.text.MessageFormat;

public class QuadTreeGeoIndex<A extends Geometry, O extends SimpleFeature> extends AbstractGeoIndex<A,O> {
    public QuadTreeGeoIndex(SimpleFeatureType sft, Attribute<O, A> attribute) {
        super(sft, attribute);
        index = new WrappedQuadtree<O>();
    }
}
