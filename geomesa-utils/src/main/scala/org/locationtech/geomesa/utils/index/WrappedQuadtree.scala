/***********************************************************************
 * Copyright (c) 2013-2019 Commonwealth Computer Research, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0
 * which accompanies this distribution and is available at
 * http://www.opensource.org/licenses/apache2.0.php.
 ***********************************************************************/

package org.locationtech.geomesa.utils.index

import org.geotools.data.shapefile.index.quadtree.QuadTree
import org.locationtech.jts.geom.{Envelope, Geometry}
import org.locationtech.jts.index.quadtree.Quadtree

import scala.collection.JavaConverters._

/**
 * Spatial index wrapper for un-synchronized quad tree
 */
class WrappedQuadtree[T] extends WrapperIndex[T,Quadtree](
  indexBuider = Function {() => new QuadTree()}

) with Serializable {

  override def query(): Iterator[T] =
    index.queryAll().iterator.asScala.asInstanceOf[Iterator[(String, T)]].map(_._2)

  override def size(): Int = index.size()

}
