/*
 * Copyright 2020 Sonu Kumar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.sonus21.rqueue.config;

import com.github.sonus21.rqueue.models.enums.RqueueMode;
import com.github.sonus21.rqueue.utils.StringUtils;
import java.net.Proxy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Getter
@Setter
@RequiredArgsConstructor
@Configuration
public class RqueueConfig {
  private final RedisConnectionFactory connectionFactory;
  private final boolean sharedConnection;
  private final int dbVersion;

  @Value("${rqueue.version:2.1.0}")
  private String version;

  @Value("${rqueue.latest.version.check.enabled:true}")
  private boolean latestVersionCheckEnabled;

  @Value("${rqueue.key.prefix:__rq::}")
  private String prefix;

  @Value("${rqueue.cluster.mode:true}")
  private boolean clusterMode;

  @Value("${rqueue.simple.queue.prefix:}")
  private String simpleQueuePrefix;

  @Value("${rqueue.delayed.queue.prefix:}")
  private String delayedQueuePrefix;

  @Value("${rqueue.delayed.queue.channel.prefix:}")
  private String delayedQueueChannelPrefix;

  @Value("${rqueue.processing.queue.name.prefix:}")
  private String processingQueuePrefix;

  @Value("${rqueue.processing.queue.channel.prefix:}")
  private String processingQueueChannelPrefix;

  @Value("${rqueue.queues.key.suffix:queues}")
  private String queuesKeySuffix;

  @Value("${rqueue.lock.key.prefix:lock::}")
  private String lockKeyPrefix;

  @Value("${rqueue.queue.stat.key.prefix:q-stat::}")
  private String queueStatKeyPrefix;

  @Value("${rqueue.queue.config.key.prefix:q-config::}")
  private String queueConfigKeyPrefix;

  @Value("${rqueue.retry.per.poll:1}")
  private int retryPerPoll;

  @Value("${rqueue.add.default.queue.with.queue.level.priority:true}")
  private boolean addDefaultQueueWithQueueLevelPriority;

  @Value("${rqueue.default.queue.with.queue.level.priority:-1}")
  private int defaultQueueWithQueueLevelPriority;

  @Value("${rqueue.net.proxy.host:}")
  private String proxyHost;

  @Value("${rqueue.net.proxy.port:8000}")
  private Integer proxyPort;

  @Value("${rqueue.net.proxy.type:HTTP}")
  private Proxy.Type proxyType;

  @Value("${rqueue.message.durability:10080}")
  private long messageDurabilityInMinute;

  @Value("${rqueue.message.durability.in-terminal-state:1800}")
  private long messageDurabilityInTerminalStateInSecond;

  @Value("${rqueue.system.mode:BOTH}")
  private RqueueMode mode;

  public String getQueuesKey() {
    return prefix + queuesKeySuffix;
  }

  private String getSimpleQueueSuffix() {
    if (!StringUtils.isEmpty(simpleQueuePrefix)) {
      return simpleQueuePrefix;
    }
    if (dbVersion == 2) {
      return "queue::";
    }
    return "queue-v2::";
  }

  private String getDelayedQueueSuffix() {
    if (!StringUtils.isEmpty(delayedQueuePrefix)) {
      return delayedQueuePrefix;
    }
    if (dbVersion == 2) {
      return "d-queue::";
    }
    return "d-queue-v2::";
  }

  private String getDelayedQueueChannelSuffix() {
    if (!StringUtils.isEmpty(delayedQueueChannelPrefix)) {
      return delayedQueueChannelPrefix;
    }
    if (dbVersion == 2) {
      return "d-channel::";
    }
    return "d-channel-v2::";
  }

  private String getProcessingQueueSuffix() {
    if (!StringUtils.isEmpty(processingQueuePrefix)) {
      return processingQueuePrefix;
    }
    if (dbVersion == 2) {
      return "p-queue::";
    }
    return "p-queue-v2::";
  }

  private String getProcessingQueueChannelSuffix() {
    if (!StringUtils.isEmpty(processingQueueChannelPrefix)) {
      return processingQueueChannelPrefix;
    }
    if (dbVersion == 2) {
      return "p-channel::";
    }
    return "p-channel-v2::";
  }

  public String getQueueName(String queueName) {
    if (dbVersion == 1) {
      return queueName;
    }
    return prefix + getSimpleQueueSuffix() + getTaggedName(queueName);
  }

  public String getDelayedQueueName(String queueName) {
    if (dbVersion == 1) {
      return "rqueue-delay::" + queueName;
    }
    return prefix + getDelayedQueueSuffix() + getTaggedName(queueName);
  }

  public String getDelayedQueueChannelName(String queueName) {
    if (dbVersion == 1) {
      return "rqueue-channel::" + queueName;
    }
    return prefix + getDelayedQueueChannelSuffix() + getTaggedName(queueName);
  }

  public String getProcessingQueueName(String queueName) {
    if (dbVersion == 1) {
      return "rqueue-processing::" + queueName;
    }
    return prefix + getProcessingQueueSuffix() + getTaggedName(queueName);
  }

  public String getProcessingQueueChannelName(String queueName) {
    if (dbVersion == 1) {
      return "rqueue-processing-channel::" + queueName;
    }
    return prefix + getProcessingQueueChannelSuffix() + getTaggedName(queueName);
  }

  public String getLockKey(String key) {
    return prefix + lockKeyPrefix + key;
  }

  public String getQueueStatisticsKey(String name) {
    return prefix + queueStatKeyPrefix + name;
  }

  public String getQueueConfigKey(String name) {
    return prefix + queueConfigKeyPrefix + name;
  }

  private String getTaggedName(String queueName) {
    if (!clusterMode) {
      return queueName;
    }
    return "{" + queueName + "}";
  }
}
