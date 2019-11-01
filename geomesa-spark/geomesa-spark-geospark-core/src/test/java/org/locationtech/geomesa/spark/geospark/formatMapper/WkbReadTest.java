package org.locationtech.geomesa.spark.geospark.formatMapper;

import org.locationtech.geomesa.spark.geospark.GeoSparkTestBase;
import org.locationtech.geomesa.spark.geospark.spatialRDD.SpatialRDD;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class WkbReadTest extends GeoSparkTestBase {
    public static String wkbGeometries = null;

    @BeforeClass
    public static void onceExecutedBeforeAll()
            throws IOException
    {
        initialize(WktReaderTest.class.getName());
        wkbGeometries = WktReaderTest.class.getClassLoader().getResource("county_small_wkb.tsv").getPath();
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
        SpatialRDD wkbRDD = WkbReader.readToGeometryRDD(sc, wkbGeometries, 0, true, false);
        assertEquals(wkbRDD.rawSpatialRDD.count(), 103);
    }
}
