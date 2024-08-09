/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.tabular.iceberg.connect.channel;

import java.util.concurrent.ExecutionException;
import org.apache.iceberg.common.DynFields;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.DescribeConsumerGroupsResult;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerGroupMetadata;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.SinkTaskContext;

public class KafkaUtils {

  private static final String CONTEXT_CLASS_NAME =
          "org.apache.kafka.connect.runtime.WorkerSinkTaskContext";

  public static ConsumerGroupDescription consumerGroupDescription(
      String consumerGroupId, Admin admin) {
    try {
      DescribeConsumerGroupsResult result =
          admin.describeConsumerGroups(ImmutableList.of(consumerGroupId));
      return result.describedGroups().get(consumerGroupId).get();

    } catch (InterruptedException | ExecutionException e) {
      throw new ConnectException(
          "Cannot retrieve members for consumer group: " + consumerGroupId, e);
    }
  }

  @SuppressWarnings("unchecked")
  public static ConsumerGroupMetadata getConsumerGroupMetadata(
          SinkTaskContext context, String connectGroupId) {
    if (CONTEXT_CLASS_NAME.equals(context.getClass().getName())) {
      return ((Consumer<byte[], byte[]>)
              DynFields.builder().hiddenImpl(CONTEXT_CLASS_NAME, "consumer").build(context).get())
              .groupMetadata();
    }
    return new ConsumerGroupMetadata(connectGroupId);
  }

  private KafkaUtils() {}
}
