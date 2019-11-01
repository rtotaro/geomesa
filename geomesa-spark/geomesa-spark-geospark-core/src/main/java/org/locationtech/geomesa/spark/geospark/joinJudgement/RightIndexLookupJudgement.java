/*
 * FILE: RightIndexLookupJudgement
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

package org.locationtech.geomesa.spark.geospark.joinJudgement;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.SpatialIndex;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.spark.api.java.function.FlatMapFunction2;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RightIndexLookupJudgement<T extends Geometry, U extends Geometry>
        extends JudgementBase
        implements FlatMapFunction2<Iterator<T>, Iterator<SpatialIndex>, Pair<T, U>>, Serializable
{

    /**
     * @see JudgementBase
     */
    public RightIndexLookupJudgement(boolean considerBoundaryIntersection, @Nullable DedupParams dedupParams)
    {
        super(considerBoundaryIntersection, dedupParams);
    }

    @Override
    public Iterator<Pair<T, U>> call(Iterator<T> streamShapes, Iterator<SpatialIndex> indexIterator)
            throws Exception
    {
        List<Pair<T, U>> result = new ArrayList<>();

        if (!indexIterator.hasNext() || !streamShapes.hasNext()) {
            return result.iterator();
        }

        initPartition();

        SpatialIndex treeIndex = indexIterator.next();
        while (streamShapes.hasNext()) {
            T streamShape = streamShapes.next();
            List<Geometry> candidates = treeIndex.query(streamShape.getEnvelopeInternal());
            for (Geometry candidate : candidates) {
                // Refine phase. Use the real polygon (instead of its MBR) to recheck the spatial relation.
                if (match(streamShape, candidate)) {
                    result.add(Pair.of(streamShape, (U) candidate));
                }
            }
        }
        return result.iterator();
    }
}
