/*
 * FILE: EarthdataMapperRunnableExample
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

import org.locationtech.jts.geom.Envelope;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.serializer.KryoSerializer;
import org.apache.spark.storage.StorageLevel;
import org.locationtech.geomesa.spark.geospark.enums.FileDataSplitter;
import org.locationtech.geomesa.spark.geospark.enums.IndexType;
import org.locationtech.geomesa.spark.geospark.formatMapper.EarthdataHDFPointMapper;
import org.locationtech.geomesa.spark.geospark.serde.GeoSparkKryoRegistrator;
import org.locationtech.geomesa.spark.geospark.spatialOperator.RangeQuery;
import org.locationtech.geomesa.spark.geospark.spatialRDD.PointRDD;

// TODO: Auto-generated Javadoc

/**
 * The Class EarthdataMapperRunnableExample.
 */
public class EarthdataMapperRunnableExample
{

    /**
     * The sc.
     */
    public static JavaSparkContext sc;

    /**
     * The Input location.
     */
    static String InputLocation;

    /**
     * The splitter.
     */
    static FileDataSplitter splitter;

    /**
     * The index type.
     */
    static IndexType indexType;

    /**
     * The num partitions.
     */
    static Integer numPartitions;

    /**
     * The query envelope.
     */
    static Envelope queryEnvelope;

    /**
     * The loop times.
     */
    static int loopTimes;

    /**
     * The HDF increment.
     */
    static int HDFIncrement = 5;

    /**
     * The HDF offset.
     */
    static int HDFOffset = 2;

    /**
     * The HDF root group name.
     */
    static String HDFRootGroupName = "MOD_Swath_LST";

    /**
     * The HDF data variable name.
     */
    static String HDFDataVariableName = "LST";

    /**
     * The HDF data variable list.
     */
    static String[] HDFDataVariableList = {"LST", "QC", "Error_LST", "Emis_31", "Emis_32"};

    /**
     * The url prefix.
     */
    static String urlPrefix = "";

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args)
    {
        SparkConf conf = new SparkConf().setAppName("EarthdataMapperRunnableExample").setMaster("local[2]");
        conf.set("spark.serializer", KryoSerializer.class.getName());
        conf.set("spark.kryo.registrator", GeoSparkKryoRegistrator.class.getName());
        sc = new JavaSparkContext(conf);
        InputLocation = System.getProperty("user.dir") + "/src/test/resources/modis/modis.csv";
        splitter = FileDataSplitter.CSV;
        indexType = IndexType.RTREE;
        queryEnvelope = new Envelope(-90.01, -80.01, 30.01, 40.01);
        numPartitions = 5;
        loopTimes = 1;
        HDFIncrement = 5;
        HDFOffset = 2;
        HDFRootGroupName = "MOD_Swath_LST";
        HDFDataVariableName = "LST";
        urlPrefix = System.getProperty("user.dir") + "/src/test/resources/modis/";
        testSpatialRangeQuery();
        testSpatialRangeQueryUsingIndex();
        sc.stop();
        System.out.println("All GeoSpark Earthdata DEMOs passed!");
    }

    /**
     * Test spatial range query.
     */
    public static void testSpatialRangeQuery()
    {
        EarthdataHDFPointMapper earthdataHDFPoint = new EarthdataHDFPointMapper(HDFIncrement, HDFOffset, HDFRootGroupName,
                HDFDataVariableList, HDFDataVariableName, urlPrefix);
        PointRDD spatialRDD = new PointRDD(sc, InputLocation, numPartitions, earthdataHDFPoint, StorageLevel.MEMORY_ONLY());
        for (int i = 0; i < loopTimes; i++) {
            long resultSize;
            try {
                resultSize = RangeQuery.SpatialRangeQuery(spatialRDD, queryEnvelope, false, false).count();
                assert resultSize > 0;
            }
            catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Test spatial range query using index.
     */
    public static void testSpatialRangeQueryUsingIndex()
    {
        EarthdataHDFPointMapper earthdataHDFPoint = new EarthdataHDFPointMapper(HDFIncrement, HDFOffset, HDFRootGroupName,
                HDFDataVariableList, HDFDataVariableName, urlPrefix);
        PointRDD spatialRDD = new PointRDD(sc, InputLocation, numPartitions, earthdataHDFPoint, StorageLevel.MEMORY_ONLY());
        try {
            spatialRDD.buildIndex(IndexType.RTREE, false);
        }
        catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        for (int i = 0; i < loopTimes; i++) {
            try {
                long resultSize;
                resultSize = RangeQuery.SpatialRangeQuery(spatialRDD, queryEnvelope, false, true).count();
                assert resultSize > 0;
            }
            catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
