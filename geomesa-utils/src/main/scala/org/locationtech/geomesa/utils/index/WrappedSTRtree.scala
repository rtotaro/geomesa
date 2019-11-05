package org.locationtech.geomesa.utils.index

import org.locationtech.jts.index.strtree.STRtree

class WrappedSTRtree[T](nodeCapacity:Int) extends WrapperIndex[T,STRtree](
  indexBuider = () => new STRtree(nodeCapacity)
) with Serializable {

  def this()
  {
    this(10)
  }

  override def size(): Int = index.size()

}