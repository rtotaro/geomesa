/*
 * FILE: KNNQuery
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
package org.locationtech.geomesa.spark.geospark.spatialOperator;

import org.locationtech.jts.geom.Geometry;
import org.apache.spark.api.java.JavaRDD;
import org.locationtech.geomesa.spark.geospark.knnJudgement.GeometryDistanceComparator;
import org.locationtech.geomesa.spark.geospark.knnJudgement.KnnJudgement;
import org.locationtech.geomesa.spark.geospark.knnJudgement.KnnJudgementUsingIndex;
import org.locationtech.geomesa.spark.geospark.spatialRDD.SpatialRDD;
import org.locationtech.geomesa.spark.geospark.utils.CRSTransformation;

import java.io.Serializable;
import java.util.List;

// TODO: Auto-generated Javadoc

/**
 * The Class KNNQuery.
 */
public class KNNQuery
        implements Serializable
{

    /**
     * Spatial knn query.
     *
     * @param spatialRDD the spatial RDD
     * @param originalQueryPoint the original query window
     * @param k the k
     * @param useIndex the use index
     * @return the list
     */
    public static <U extends Geometry, T extends Geometry> List<T> SpatialKnnQuery(SpatialRDD<T> spatialRDD, U originalQueryPoint, Integer k, boolean useIndex)
    {
        U queryCenter = originalQueryPoint;
        if (spatialRDD.getCRStransformation()) {
            queryCenter = CRSTransformation.Transform(spatialRDD.getSourceEpsgCode(), spatialRDD.getTargetEpgsgCode(), originalQueryPoint);
        }

        if (useIndex) {
            if (spatialRDD.indexedRawRDD == null) {
                throw new NullPointerException("Need to invoke buildIndex() first, indexedRDDNoId is null");
            }
            JavaRDD<T> tmp = spatialRDD.indexedRawRDD.mapPartitions(new KnnJudgementUsingIndex(queryCenter, k));
            List<T> result = tmp.takeOrdered(k, new GeometryDistanceComparator(queryCenter, true));
            // Take the top k
            return result;
        }
        else {
            JavaRDD<T> tmp = spatialRDD.getRawSpatialRDD().mapPartitions(new KnnJudgement(queryCenter, k));
            List<T> result = tmp.takeOrdered(k, new GeometryDistanceComparator(queryCenter, true));
            // Take the top k
            return result;
        }
    }
}
