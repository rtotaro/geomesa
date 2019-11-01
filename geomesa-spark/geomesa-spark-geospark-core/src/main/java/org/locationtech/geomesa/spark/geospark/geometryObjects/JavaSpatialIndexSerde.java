package org.locationtech.geomesa.spark.geospark.geometryObjects;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

public class JavaSpatialIndexSerde extends Serializer {

    @Override
    public void write(Kryo kryo, Output output, Object o) {
        if (o instanceof Serializable) {
            Serializable s = (Serializable) o;
            SerializationUtils.serialize(s, output);
        } else {
            throw new IllegalArgumentException("Not serializable object " + o.toString());
        }
    }

    @Override
    public Object read(Kryo kryo, Input input, Class aClass) {
        return SerializationUtils.deserialize(input);
    }
}
