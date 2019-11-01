/*
 * FILE: GeoJsonReaderTest
 * Copyright (c) 2015 - 2018 GeoSpark Development Team
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package org.locationtech.geomesa.spark.geospark.formatMapper;

import org.locationtech.geomesa.spark.geospark.GeoSparkTestBase;
import org.locationtech.geomesa.spark.geospark.spatialRDD.SpatialRDD;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class GeoJsonReaderTest
        extends GeoSparkTestBase
{

    public static String geoJsonGeomWithFeatureProperty = null;
    public static String geoJsonGeomWithoutFeatureProperty = null;
    public static String geoJsonWithInvalidGeometries = null;
    public static String geoJsonContainsId = null;

    @BeforeClass
    public static void onceExecutedBeforeAll()
            throws IOException
    {
        initialize(GeoJsonReaderTest.class.getName());
        geoJsonGeomWithFeatureProperty = GeoJsonReaderTest.class.getClassLoader().getResource("testPolygon.json").getPath();
        geoJsonGeomWithoutFeatureProperty = GeoJsonReaderTest.class.getClassLoader().getResource("testpolygon-no-property.json").getPath();
        geoJsonWithInvalidGeometries = GeoJsonReaderTest.class.getClassLoader().getResource("testInvalidPolygon.json").getPath();
        geoJsonContainsId = GeoJsonReaderTest.class.getClassLoader().getResource("testContainsId.json").getPath();
    }

    @AfterClass
    public static void tearDown()
            throws Exception
    {
        sc.stop();
    }

    /**
     * Test correctness of parsing geojson file
     *
     * @throws IOException
     */
    @Test
    public void testReadToGeometryRDD()
            throws IOException
    {
        // load geojson with our tool
        SpatialRDD geojsonRDD = GeoJsonReader.readToGeometryRDD(sc, geoJsonGeomWithFeatureProperty);
        assertEquals(geojsonRDD.rawSpatialRDD.count(), 1001);
        geojsonRDD = GeoJsonReader.readToGeometryRDD(sc, geoJsonGeomWithoutFeatureProperty);
        assertEquals(geojsonRDD.rawSpatialRDD.count(), 10);
    }

    /**
     * Test correctness of parsing geojson file
     *
     * @throws IOException
     */
    @Test
    public void testReadToValidGeometryRDD()
            throws IOException
    {
        //ensure that flag does not affect valid geometries
        SpatialRDD geojsonRDD = GeoJsonReader.readToGeometryRDD(sc, geoJsonGeomWithFeatureProperty, true, false);
        assertEquals(geojsonRDD.rawSpatialRDD.count(), 1001);
        geojsonRDD = GeoJsonReader.readToGeometryRDD(sc, geoJsonGeomWithoutFeatureProperty, true, false);
        assertEquals(geojsonRDD.rawSpatialRDD.count(), 10);
        //2 valid and 1 invalid geometries
        geojsonRDD = GeoJsonReader.readToGeometryRDD(sc, geoJsonWithInvalidGeometries, false,false);
        assertEquals(geojsonRDD.rawSpatialRDD.count(), 2);

        geojsonRDD = GeoJsonReader.readToGeometryRDD(sc, geoJsonWithInvalidGeometries);
        assertEquals(geojsonRDD.rawSpatialRDD.count(), 3);
    }

    /**
     * Test correctness of parsing geojson file including id
     */
    @Test
    public void testReadToIncludIdRDD() throws IOException
    {
        SpatialRDD geojsonRDD = GeoJsonReader.readToGeometryRDD(sc,geoJsonContainsId,true,false);
        assertEquals(geojsonRDD.rawSpatialRDD.count(),1);
        assertEquals(geojsonRDD.fieldNames.size(), 3);
    }
}
