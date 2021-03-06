/*
 * Copyright 2017 StreamSets Inc.
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
package com.streamsets.pipeline.stage.origin.maprstreams;

import com.streamsets.pipeline.api.ExecutionMode;
import com.streamsets.pipeline.api.GenerateResourceBundle;
import com.streamsets.pipeline.api.HideConfigs;
import com.streamsets.pipeline.api.RawSource;
import com.streamsets.pipeline.api.StageDef;

import com.streamsets.pipeline.stage.origin.multikafka.MultiKafkaDSource;
import com.streamsets.pipeline.stage.origin.multikafka.MultiKafkaRawSourcePreviewer;

@StageDef(
    version = 1,
    label = "MapR Multitopic Streams Consumer",
    description = "Reads data from multiple topics of a MapR streams",
    execution = ExecutionMode.STANDALONE,
    icon = "mapr_es.png",
    recordsByRef = true,
    onlineHelpRefUrl ="index.html?contextID=task_pkc_lww_lbb"
)
@RawSource(rawSourcePreviewer = MultiKafkaRawSourcePreviewer.class,  mimeType = "*/*")
@HideConfigs({
  "conf.brokerURI"
})
@GenerateResourceBundle
public class MultiMapRStreamsDSource extends MultiKafkaDSource {
}
