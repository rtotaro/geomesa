/*
 * FILE: UserSuppliedLineStringMapper
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
package org.locationtech.geomesa.spark.geospark.showcase;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;
import org.apache.spark.api.java.function.FlatMapFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

// TODO: Auto-generated Javadoc

/**
 * The Class UserSuppliedLineStringMapper.
 */
public class UserSuppliedLineStringMapper
        implements FlatMapFunction<Iterator<String>, Object>
{

    /**
     * The spatial object.
     */
    Geometry spatialObject = null;

    /**
     * The multi spatial objects.
     */
    MultiPolygon multiSpatialObjects = null;

    /**
     * The fact.
     */
    GeometryFactory fact = new GeometryFactory();

    /**
     * The line split list.
     */
    List<String> lineSplitList;

    /**
     * The coordinates list.
     */
    ArrayList<Coordinate> coordinatesList;

    /**
     * The coordinates.
     */
    Coordinate[] coordinates;

    /**
     * The linear.
     */
    LinearRing linear;

    /**
     * The actual end offset.
     */
    int actualEndOffset;

    @Override
    public Iterator<Object> call(Iterator<String> stringIterator)
            throws Exception
    {
        List result = new ArrayList<LineString>();
        while (stringIterator.hasNext()) {
            String line = stringIterator.next();
            Geometry spatialObject = null;
            MultiLineString multiSpatialObjects = null;
            List<String> lineSplitList;
            lineSplitList = Arrays.asList(line.split("\t"));
            String newLine = lineSplitList.get(0).replace("\"", "");
            WKTReader wktreader = new WKTReader();
            spatialObject = wktreader.read(newLine);
            if (spatialObject instanceof MultiLineString) {
                multiSpatialObjects = (MultiLineString) spatialObject;
                for (int i = 0; i < multiSpatialObjects.getNumGeometries(); i++) {
                    spatialObject = multiSpatialObjects.getGeometryN(i);
                    result.add((LineString) spatialObject);
                }
            }
            else {
                result.add((LineString) spatialObject);
            }
        }
        return result.iterator();
    }
}
