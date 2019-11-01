/*
 * FILE: PolygonParser
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

import org.locationtech.jts.geom.*;
import org.geotools.geometry.jts.coordinatesequence.CoordinateSequences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.locationtech.geomesa.spark.geospark.formatMapper.shapefileParser.parseUtils.shp.ShapeFileConst.DOUBLE_LENGTH;

public class PolygonParser
        extends ShapeParser
{

    /**
     * create a parser that can abstract a Polygon from input source with given GeometryFactory.
     *
     * @param geometryFactory the geometry factory
     */
    public PolygonParser(GeometryFactory geometryFactory)
    {
        super(geometryFactory);
    }

    /**
     * abstract abstract a Polygon shape.
     *
     * @param reader the reader
     * @return the geometry
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public Geometry parseShape(ShapeReader reader)
    {
        reader.skip(4 * DOUBLE_LENGTH);

        int numRings = reader.readInt();
        int numPoints = reader.readInt();

        int[] offsets = readOffsets(reader, numRings, numPoints);

        boolean shellsCCW = false;

        LinearRing shell = null;
        List<LinearRing> holes = new ArrayList<>();
        List<Polygon> polygons = new ArrayList<>();

        for (int i = 0; i < numRings; ++i) {
            int readScale = offsets[i + 1] - offsets[i];
            CoordinateSequence csRing = readCoordinates(reader, readScale);

            if (csRing.size() <= 3) {
                continue; // if points less than 3, it's not a ring, we just abandon it
            }

            LinearRing ring = geometryFactory.createLinearRing(csRing);
            if (shell == null) {
                shell = ring;
                shellsCCW = CoordinateSequences.isCCW(csRing);
            }
            else if (CoordinateSequences.isCCW(csRing) != shellsCCW) {
                holes.add(ring);
            }
            else {
                if (shell != null) {
                    Polygon polygon = geometryFactory.createPolygon(shell, GeometryFactory.toLinearRingArray(holes));
                    polygons.add(polygon);
                }

                shell = ring;
                holes.clear();
            }
        }

        if (shell != null) {
            Polygon polygon = geometryFactory.createPolygon(shell, GeometryFactory.toLinearRingArray(holes));
            polygons.add(polygon);
        }

        if (polygons.size() == 1) {
            return polygons.get(0);
        }

        return geometryFactory.createMultiPolygon(polygons.toArray(new Polygon[0]));
    }
}
