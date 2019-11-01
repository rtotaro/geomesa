package org.locationtech.geomesa.spark.geospark.formatMapper;

import org.locationtech.geomesa.spark.geospark.GeoSparkTestBase;
import org.locationtech.geomesa.spark.geospark.spatialRDD.SpatialRDD;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TestReadInvalidSyntaxGeometriesTest extends GeoSparkTestBase {

    public static String invalidSyntaxGeoJsonGeomWithFeatureProperty = null;

    @BeforeClass
    public static void onceExecutedBeforeAll()
            throws IOException
    {
        initialize(GeoJsonReaderTest.class.getName());
        invalidSyntaxGeoJsonGeomWithFeatureProperty = TestReadInvalidSyntaxGeometriesTest.class.getClassLoader().getResource("invalidSyntaxGeometriesJson.json").getPath();
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
        // would crash with java.lang.IllegalArgumentException: Points of LinearRing do not form a closed linestring if Invalid syntax is not skipped
        SpatialRDD geojsonRDD = GeoJsonReader.readToGeometryRDD(sc, invalidSyntaxGeoJsonGeomWithFeatureProperty, false, true);
        assertEquals(geojsonRDD.rawSpatialRDD.count(), 1);
    }

}
