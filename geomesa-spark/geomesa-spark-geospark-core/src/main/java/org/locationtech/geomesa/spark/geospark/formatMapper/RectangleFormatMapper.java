/*
 * FILE: RectangleFormatMapper
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
package org.locationtech.geomesa.spark.geospark.formatMapper;

import org.locationtech.geomesa.spark.geospark.enums.FileDataSplitter;
import org.locationtech.geomesa.spark.geospark.enums.GeometryType;

public class RectangleFormatMapper
        extends FormatMapper
{

    /**
     * Instantiates a new rectangle format mapper.
     *
     * @param Splitter the splitter
     * @param carryInputData the carry input data
     */
    public RectangleFormatMapper(FileDataSplitter Splitter, boolean carryInputData)
    {
        super(0, 3, Splitter, carryInputData, GeometryType.RECTANGLE);
    }

    /**
     * Instantiates a new rectangle format mapper.
     *
     * @param startOffset the start offset
     * @param Splitter the splitter
     * @param carryInputData the carry input data
     */
    public RectangleFormatMapper(Integer startOffset, FileDataSplitter Splitter,
            boolean carryInputData)
    {
        super(startOffset, startOffset+3, Splitter, carryInputData, GeometryType.RECTANGLE);
    }
}
