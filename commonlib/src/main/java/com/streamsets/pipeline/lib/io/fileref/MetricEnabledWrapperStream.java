/**
 * Copyright 2016 StreamSets Inc.
 *
 * Licensed under the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.streamsets.pipeline.lib.io.fileref;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.streamsets.pipeline.api.Stage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.Map;

/**
 * The Implementation of {@link AbstractWrapperStream} which maintains the metrics
 * for the stream read.
 * @param <T> Stream implementation of {@link AutoCloseable}
 */
final class MetricEnabledWrapperStream<T extends AutoCloseable> extends AbstractWrapperStream<T> {
  private final Meter dataThroughputMeterForCurrentStream;
  private final Meter dataTransferMeter;
  private final Counter remainingBytesCounter;
  private final Counter copiedBytesCounter;
  private final Map<String, Object> gaugeStatisticsMap;
  private static final String[] UNITS = new String[]{"B", "KB", "MB", "GB", "TB"};
  private static final DecimalFormat df = new DecimalFormat("#.##");
  private static final String PER_SEC = "/s";

  @SuppressWarnings("unchecked")
  MetricEnabledWrapperStream(String id, long fileSize, Stage.Context context, T stream) {
    super(stream);
    dataThroughputMeterForCurrentStream = new Meter();
    remainingBytesCounter = new Counter();
    copiedBytesCounter = new Counter();
    remainingBytesCounter.inc(fileSize);
    dataTransferMeter = context.getMeter(FileRefUtil.TRANSFER_THROUGHPUT_METER);
    gaugeStatisticsMap =  (Map<String, Object>)context.getGauge(FileRefUtil.GAUGE_NAME).getValue();
    gaugeStatisticsMap.put(FileRefUtil.FILE_NAME, id);
  }

  private int updateMetricsAndReturnBytesRead(int bytesRead) {
    if (bytesRead > 0) {
      dataThroughputMeterForCurrentStream.mark(bytesRead);
      //In KB
      dataTransferMeter.mark(bytesRead);
      copiedBytesCounter.inc(bytesRead);
      remainingBytesCounter.dec(bytesRead);
      //Putting one minute rate because that is the latest speed of transfer
      gaugeStatisticsMap.put(
          FileRefUtil.TRANSFER_THROUGHPUT,
          convertBytesToDisplayFormat(dataThroughputMeterForCurrentStream.getOneMinuteRate()) + PER_SEC
      );
      gaugeStatisticsMap.put(
          FileRefUtil.COPIED_BYTES,
          convertBytesToDisplayFormat((double)copiedBytesCounter.getCount())
      );
      gaugeStatisticsMap.put(
          FileRefUtil.REMAINING_BYTES,
          convertBytesToDisplayFormat((double)remainingBytesCounter.getCount())
      );
    }
    return bytesRead;
  }

  /**
   * Convert the bytes to a human readable format upto 2 decimal places
   * The maximum unit is TB, so anything exceeding 1024 TB will be shown
   * with TB unit.
   * @param bytes the number of bytes.
   * @return human readable format of bytes in units.
   */
  static String convertBytesToDisplayFormat(double bytes) {
    int unitIdx = 0;
    double unitChangedBytes = bytes;
    while (unitIdx < UNITS.length - 1 && Math.floor(unitChangedBytes / 1024) > 0) {
      unitChangedBytes = unitChangedBytes / 1024;
      unitIdx++;
    }
    return df.format(unitChangedBytes) + " " + UNITS[unitIdx];
  }

  @Override
  public int read() throws IOException {
    int readByte = super.read();
    updateMetricsAndReturnBytesRead((readByte != -1)? 1 : 0);
    return readByte;
  }

  @Override
  public int read(ByteBuffer dst) throws IOException {
    return updateMetricsAndReturnBytesRead(super.read(dst));
  }

  @Override
  public int read(byte[] b) throws IOException {
    return updateMetricsAndReturnBytesRead(super.read(b));
  }

  @Override
  public int read(byte[] b, int offset, int len) throws IOException {
    return updateMetricsAndReturnBytesRead(super.read(b, offset, len));
  }
}
