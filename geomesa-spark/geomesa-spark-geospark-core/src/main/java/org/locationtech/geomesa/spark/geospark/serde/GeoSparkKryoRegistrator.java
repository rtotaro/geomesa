/*
 * FILE: GeoSparkKryoRegistrator
 * Copyright (c) 2015 - 2019 GeoSpark Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.locationtech.geomesa.spark.geospark.serde;

import com.esotericsoftware.kryo.Kryo;
import org.locationtech.geomesa.spark.geospark.geometryObjects.JavaSpatialIndexSerde;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.index.quadtree.Node;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.locationtech.jts.index.strtree.STRtree;
import org.apache.spark.serializer.KryoRegistrator;
import org.locationtech.geomesa.spark.geospark.geometryObjects.Circle;
import org.locationtech.geomesa.spark.geospark.geometryObjects.GeometrySerde;
//import org.locationtech.geomesa.spark.geospark.geometryObjects.SpatialIndexSerde;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeoSparkKryoRegistrator
        implements KryoRegistrator
{

    final static Logger log = LoggerFactory.getLogger(GeoSparkKryoRegistrator.class);

    @Override
    public void registerClasses(Kryo kryo)
    {
        GeometrySerde serializer = new GeometrySerde();
//        SpatialIndexSerde indexSerializer = new SpatialIndexSerde(serializer);

        log.info("Registering custom serializers for geometry types");

        kryo.register(Point.class, serializer);
        kryo.register(LineString.class, serializer);
        kryo.register(Polygon.class, serializer);
        kryo.register(MultiPoint.class, serializer);
        kryo.register(MultiLineString.class, serializer);
        kryo.register(MultiPolygon.class, serializer);
        kryo.register(GeometryCollection.class, serializer);
        kryo.register(Circle.class, serializer);
        kryo.register(Envelope.class, serializer);
        // TODO: Replace the default serializer with default spatial index serializer
        kryo.register(Quadtree.class, new JavaSpatialIndexSerde());
        kryo.register(STRtree.class, new JavaSpatialIndexSerde());

    }
}
