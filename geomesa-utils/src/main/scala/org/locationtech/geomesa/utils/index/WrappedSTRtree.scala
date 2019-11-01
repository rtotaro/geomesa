package org.locationtech.geomesa.utils.index

import org.locationtech.jts.index.strtree.STRtree

class WrappedSTRtree[T]  extends WrapperIndex[T,STRtree](
  indexBuider = () => new STRtree()

) with Serializable {

  override def size(): Int = index.size()

}