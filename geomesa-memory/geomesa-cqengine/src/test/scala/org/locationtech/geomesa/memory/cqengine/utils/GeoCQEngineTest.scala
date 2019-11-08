/** *********************************************************************
  * Copyright (c) 2013-2019 Commonwealth Computer Research, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Apache License, Version 2.0
  * which accompanies this distribution and is available at
  * http://www.opensource.org/licenses/apache2.0.php.
  * **********************************************************************/

/** *********************************************************************
  * Copyright (c) 2013-2019 Commonwealth Computer Research, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Apache License, Version 2.0
  * which accompanies this distribution and is available at
  * http://www.opensource.org/licenses/apache2.0.php.
  * **********************************************************************/

package org.locationtech.geomesa.memory.cqengine.utils

import java.lang

import com.typesafe.scalalogging.LazyLogging
import org.apache.log4j
import org.apache.log4j.spi.LoggingEvent
import org.apache.log4j.{AppenderSkeleton, Category, Level, LogManager, Logger}
import org.junit.runner.RunWith
import org.locationtech.geomesa.memory.cqengine.{GeoCQEngine, index}
import org.locationtech.geomesa.memory.cqengine.attribute.{GeoIndexType, STRtreeIndexParam}
import org.locationtech.geomesa.memory.cqengine.index.{AbstractGeoIndex, BucketGeoIndex, QuadTreeGeoIndex, STRtreeGeoIndex}
import org.locationtech.geomesa.memory.cqengine.utils.SampleFeatures._
import org.locationtech.geomesa.utils.collection.SelfClosingIterator
import org.locationtech.geomesa.utils.index.{SpatialIndex, WrappedQuadtree, WrappedSTRtree}
import org.opengis.feature.simple.SimpleFeature
import org.opengis.filter.Filter
import org.opengis.geometry.Geometry
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import org.specs2.specification.core.{Fragment, Fragments}

import scala.reflect.ClassTag

@RunWith(classOf[JUnitRunner])
class GeoCQEngineTest extends Specification with LazyLogging {

  import SampleFilters._

  System.setProperty("GeoCQEngineDebugEnabled", "true")

  val feats = (0 until 1000).map(SampleFeatures.buildFeature)

  // Set up CQEngine with no indexes
  val cqNoIndexes = new GeoCQEngine(sft, Seq.empty)
  cqNoIndexes.insert(feats)

  // Set up CQEngine with all indexes
  val cqWithIndexes = new GeoCQEngine(sftWithIndexes, CQIndexType.getDefinedAttributes(sftWithIndexes), enableFidIndex = true)
  cqWithIndexes.insert(feats)

  // Set up CQEngine with rtree indexes
  val cqWithSTRtreeIndexes = new GeoCQEngine(sftWithIndexes, CQIndexType.getDefinedAttributes(sftWithIndexes), enableFidIndex = true, GeoIndexType.STRtree)
  cqWithSTRtreeIndexes.insert(feats)

  // Set up CQEngine with STRtree indexes and node capacity
  val cqWithSTRtreeNodeCapacityIndexes = new GeoCQEngine(sftWithIndexes, CQIndexType.getDefinedAttributes(sftWithIndexes), enableFidIndex = true, GeoIndexType.STRtree, Option.apply(new STRtreeIndexParam(20)))
  cqWithSTRtreeNodeCapacityIndexes.insert(feats)

  // Set up CQEngine with Quadtree indexes
  val cqWithQuadtreeIndexes = new GeoCQEngine(sftWithIndexes, CQIndexType.getDefinedAttributes(sftWithIndexes), enableFidIndex = true, GeoIndexType.QuadTree)
  cqWithQuadtreeIndexes.insert(feats)


  def getGeoToolsCount(filter: Filter) = feats.count(filter.evaluate)

  def getCQEngineCount(filter: Filter, cq: GeoCQEngine) = {
    SelfClosingIterator(cq.query(filter)).size
  }

  def checkFilter(filter: Filter, cq: GeoCQEngine, spatialIndex: Option[Class[_]]): MatchResult[Int] = {
    val gtCount = getGeoToolsCount(filter)

    val cqCount = getCQEngineCount(filter, cq)


    val lastIndexUsed = GeoCQEngine.getLastIndexUsed()
    if (spatialIndex.isDefined) {
      val expected = spatialIndex.get
      lastIndexUsed must beAnInstanceOf expected
    }

    val msg = s"GT: $gtCount CQ: $cqCount Filter: $filter"
    if (gtCount == cqCount)
      logger.debug(msg)
    else
      logger.error("MISMATCH: " + msg)

    // since GT count is (presumably) correct
    cqCount must equalTo(gtCount)
  }

  def buildFilterTests(name: String, filters: Seq[Filter], checkFilterType: Boolean): Seq[Fragment] = {

    var spatialIndex: Option[Class[_] ]= Option.empty
    for (f <- filters) yield {

      s"return correct number of results for $name filter $f (geo-only index)" >> {
        checkFilter(f, cqNoIndexes, spatialIndex)
      }
      s"return correct number of results for $name filter $f (various indices)" >> {
        if (checkFilterType) {
          spatialIndex = Option.apply(classOf[BucketGeoIndex[_, _]])
        }
        checkFilter(f, cqWithIndexes, spatialIndex)
      }
      s"return correct number of results for $name filter $f (various with geo-SRTree with default config)" >> {
        if (checkFilterType) {
          spatialIndex = Option.apply(classOf[WrappedSTRtree[_]])
        }
        checkFilter(f, cqWithSTRtreeIndexes, spatialIndex)
      }
      s"return correct number of results for $name filter $f (various with geo-SRTree with nodecapacity=20)" >> {
        if (checkFilterType) {
          spatialIndex = Option.apply(classOf[WrappedSTRtree[_]])
        }
        checkFilter(f, cqWithSTRtreeNodeCapacityIndexes, spatialIndex)
      }
      s"return correct number of results for $name filter $f (various with QuadTree)" >> {
        if (checkFilterType) {
          spatialIndex = Option.apply(classOf[WrappedQuadtree[_]])
        }
        checkFilter(f, cqWithQuadtreeIndexes, spatialIndex)
      }
    }
  }

  def runFilterTests(name: String, filters: Seq[Filter], checkFilter: Boolean): Fragments = Fragments(buildFilterTests(name, filters, checkFilter): _*)

  "GeoCQEngine" should {
    runFilterTests("equality", equalityFilters, false)
    runFilterTests("special", specialFilters, false)
    runFilterTests("null", nullFilters, false)
    runFilterTests("comparable", comparableFilters, false)
    runFilterTests("temporal", temporalFilters, false)
    runFilterTests("one level AND", oneLevelAndFilters, false)
    runFilterTests("one level multiple AND", oneLevelMultipleAndsFilters, false)
    runFilterTests("one level OR", oneLevelOrFilters, false)
    runFilterTests("one level multiple OR", oneLevelMultipleOrsFilters, false)
    runFilterTests("one level NOT", simpleNotFilters, false)
    runFilterTests("basic spatial predicates", spatialPredicates, true)
    runFilterTests("attribute predicates", attributePredicates, false)
    runFilterTests("function predicates", functionPredicates, false)
  }
}
