/*
 * FILE: MultiPointParser
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
package org.locationtech.geomesa.spark.geospark.formatMapper.shapefileParser.parseUtils.shp;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPoint;

import java.io.IOException;

import static org.locationtech.geomesa.spark.geospark.formatMapper.shapefileParser.parseUtils.shp.ShapeFileConst.DOUBLE_LENGTH;

public class MultiPointParser
        extends ShapeParser
{

    /**
     * create a parser that can abstract a MultiPoint from input source with given GeometryFactory.
     *
     * @param geometryFactory the geometry factory
     */
    public MultiPointParser(GeometryFactory geometryFactory)
    {
        super(geometryFactory);
    }

    /**
     * abstract a MultiPoint shape.
     *
     * @param reader the reader
     * @return the geometry
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public Geometry parseShape(ShapeReader reader)
    {
        reader.skip(4 * DOUBLE_LENGTH);
        int numPoints = reader.readInt();
        CoordinateSequence coordinateSequence = readCoordinates(reader, numPoints);
        MultiPoint multiPoint = geometryFactory.createMultiPoint(coordinateSequence);
        return multiPoint;
    }
}
