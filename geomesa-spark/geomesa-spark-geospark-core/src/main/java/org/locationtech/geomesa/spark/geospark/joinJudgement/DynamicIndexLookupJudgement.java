/*
 * FILE: DynamicIndexLookupJudgement
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

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.SpatialIndex;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.locationtech.jts.index.strtree.STRtree;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.spark.TaskContext;
import org.apache.spark.api.java.function.FlatMapFunction2;
import org.locationtech.geomesa.spark.geospark.enums.IndexType;
import org.locationtech.geomesa.spark.geospark.enums.JoinBuildSide;
import org.locationtech.geomesa.spark.geospark.monitoring.GeoSparkMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;

import static org.locationtech.geomesa.spark.geospark.utils.TimeUtils.elapsedSince;

public class DynamicIndexLookupJudgement<T extends Geometry, U extends Geometry>
        extends JudgementBase
        implements FlatMapFunction2<Iterator<U>, Iterator<T>, Pair<U, T>>, Serializable
{

    private static final Logger log = LoggerFactory.getLogger(DynamicIndexLookupJudgement.class);

    private final IndexType indexType;
    private final JoinBuildSide joinBuildSide;
    private final GeoSparkMetric buildCount;
    private final GeoSparkMetric streamCount;
    private final GeoSparkMetric resultCount;
    private final GeoSparkMetric candidateCount;

    /**
     * @see JudgementBase
     */
    public DynamicIndexLookupJudgement(boolean considerBoundaryIntersection,
            IndexType indexType,
            JoinBuildSide joinBuildSide,
            @Nullable DedupParams dedupParams,
            GeoSparkMetric buildCount,
            GeoSparkMetric streamCount,
            GeoSparkMetric resultCount,
            GeoSparkMetric candidateCount)
    {
        super(considerBoundaryIntersection, dedupParams);
        this.indexType = indexType;
        this.joinBuildSide = joinBuildSide;
        this.buildCount = buildCount;
        this.streamCount = streamCount;
        this.resultCount = resultCount;
        this.candidateCount = candidateCount;
    }

    @Override
    public Iterator<Pair<U, T>> call(final Iterator<U> leftShapes, final Iterator<T> rightShapes)
            throws Exception
    {

        if (!leftShapes.hasNext() || !rightShapes.hasNext()) {
            buildCount.add(0);
            streamCount.add(0);
            resultCount.add(0);
            candidateCount.add(0);
            return Collections.emptyIterator();
        }

        initPartition();

        final boolean buildLeft = (joinBuildSide == JoinBuildSide.LEFT);

        final Iterator<? extends Geometry> buildShapes;
        final Iterator<? extends Geometry> streamShapes;
        if (buildLeft) {
            buildShapes = leftShapes;
            streamShapes = rightShapes;
        }
        else {
            buildShapes = rightShapes;
            streamShapes = leftShapes;
        }

        final SpatialIndex spatialIndex = buildIndex(buildShapes);

        return new Iterator<Pair<U, T>>()
        {
            // A batch of pre-computed matches
            private List<Pair<U, T>> batch = null;
            // An index of the element from 'batch' to return next
            private int nextIndex = 0;

            private int shapeCnt = 0;

            @Override
            public boolean hasNext()
            {
                if (batch != null) {
                    return true;
                }
                else {
                    return populateNextBatch();
                }
            }

            @Override
            public Pair<U, T> next()
            {
                if (batch == null) {
                    populateNextBatch();
                }

                if (batch != null) {
                    final Pair<U, T> result = batch.get(nextIndex);
                    nextIndex++;
                    if (nextIndex >= batch.size()) {
                        populateNextBatch();
                        nextIndex = 0;
                    }
                    return result;
                }

                throw new NoSuchElementException();
            }

            private boolean populateNextBatch()
            {
                if (!streamShapes.hasNext()) {
                    if (batch != null) {
                        batch = null;
                    }
                    return false;
                }

                batch = new ArrayList<>();

                while (streamShapes.hasNext()) {
                    shapeCnt++;
                    streamCount.add(1);
                    final Geometry streamShape = streamShapes.next();
                    final List candidates = spatialIndex.query(streamShape.getEnvelopeInternal());
                    for (Object candidate : candidates) {
                        candidateCount.add(1);
                        final Geometry buildShape = (Geometry) candidate;
                        if (buildLeft) {
                            if (match(buildShape, streamShape)) {
                                batch.add(Pair.of((U) buildShape, (T) streamShape));
                                resultCount.add(1);
                            }
                        }
                        else {
                            if (match(streamShape, buildShape)) {
                                batch.add(Pair.of((U) streamShape, (T) buildShape));
                                resultCount.add(1);
                            }
                        }
                    }
                    logMilestone(shapeCnt, 100 * 1000, "Streaming shapes");
                    if (!batch.isEmpty()) {
                        return true;
                    }
                }

                batch = null;
                return false;
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    private SpatialIndex buildIndex(Iterator<? extends Geometry> geometries)
    {
        long startTime = System.currentTimeMillis();
        long count = 0;
        final SpatialIndex index = newIndex();
        while (geometries.hasNext()) {
            Geometry geometry = geometries.next();
            index.insert(geometry.getEnvelopeInternal(), geometry);
            count++;
        }
        index.query(new Envelope(0.0, 0.0, 0.0, 0.0));
        log("Loaded %d shapes into an index in %d ms", count, elapsedSince(startTime));
        buildCount.add((int) count);
        return index;
    }

    private SpatialIndex newIndex()
    {
        switch (indexType) {
            case RTREE:
                return new STRtree();
            case QUADTREE:
                return new Quadtree();
            default:
                throw new IllegalArgumentException("Unsupported index type: " + indexType);
        }
    }

    private void log(String message, Object... params)
    {
        if (log.isInfoEnabled()) {
            final int partitionId = TaskContext.getPartitionId();
            final long threadId = Thread.currentThread().getId();
            log.info("[" + threadId + ", PID=" + partitionId + "] " + String.format(message, params));
        }
    }

    private void logMilestone(long cnt, long threshold, String name)
    {
        if (cnt > 1 && cnt % threshold == 1) {
            log("[%s] Reached a milestone: %d", name, cnt);
        }
    }
}
