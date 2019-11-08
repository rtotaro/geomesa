/** *********************************************************************
  * Copyright (c) 2013-2019 Commonwealth Computer Research, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Apache License, Version 2.0
  * which accompanies this distribution and is available at
  * http://www.opensource.org/licenses/apache2.0.php.
  * **********************************************************************/

package org.locationtech.geomesa.memory.cqengine.utils

import com.typesafe.scalalogging.LazyLogging
import org.junit.runner.RunWith
import org.locationtech.geomesa.memory.cqengine.GeoCQEngine
import org.locationtech.geomesa.memory.cqengine.attribute.{GeoIndexType, STRtreeIndexParam}
import org.locationtech.geomesa.memory.cqengine.utils.SampleFeatures._
import org.locationtech.geomesa.utils.collection.SelfClosingIterator
import org.opengis.filter.Filter
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import org.specs2.specification.core.{Fragment, Fragments}

@RunWith(classOf[JUnitRunner])
class GeoCQEngineTest extends Specification with LazyLogging {

  import SampleFilters._

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

  def checkFilter(filter: Filter, cq: GeoCQEngine): MatchResult[Int] = {
    val gtCount = getGeoToolsCount(filter)

    val cqCount = getCQEngineCount(filter, cq)

    val msg = s"GT: $gtCount CQ: $cqCount Filter: $filter"
    if (gtCount == cqCount)
      logger.debug(msg)
    else
      logger.error("MISMATCH: " + msg)

    // since GT count is (presumably) correct
    cqCount must equalTo(gtCount)
  }

  def buildFilterTests(name: String, filters: Seq[Filter]): Seq[Fragment] = {
    for (f <- filters) yield {

      s"return correct number of results for $name filter $f (geo-only index)" >> {
        checkFilter(f, cqNoIndexes)
      }
      s"return correct number of results for $name filter $f (various indices)" >> {
        checkFilter(f, cqWithIndexes)
      }
      s"return correct number of results for $name filter $f (various with geo-SRTree with default config)" >> {
        checkFilter(f, cqWithSTRtreeIndexes)
      }
      s"return correct number of results for $name filter $f (various with geo-SRTree with nodecapacity=20)" >> {
        checkFilter(f, cqWithSTRtreeNodeCapacityIndexes)
      }
      s"return correct number of results for $name filter $f (various with QuadTree)" >> {
        checkFilter(f, cqWithQuadtreeIndexes)
      }
    }
  }

  def runFilterTests(name: String, filters: Seq[Filter], checkFilter: Boolean): Fragments = Fragments(buildFilterTests(name, filters): _*)

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
