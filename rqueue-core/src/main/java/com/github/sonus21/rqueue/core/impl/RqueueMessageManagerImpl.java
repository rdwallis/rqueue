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

package com.github.sonus21.rqueue.core.impl;

import static org.springframework.util.Assert.notEmpty;

import com.github.sonus21.rqueue.converter.GenericMessageConverter;
import com.github.sonus21.rqueue.core.EndpointRegistry;
import com.github.sonus21.rqueue.core.RqueueMessage;
import com.github.sonus21.rqueue.core.RqueueMessageManager;
import com.github.sonus21.rqueue.core.RqueueMessageTemplate;
import com.github.sonus21.rqueue.listener.QueueDetail;
import com.github.sonus21.rqueue.listener.RqueueMessageHeaders;
import com.github.sonus21.rqueue.models.db.MessageMetadata;
import com.github.sonus21.rqueue.models.db.TaskStatus;
import com.github.sonus21.rqueue.utils.MessageUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.support.MessageBuilder;

@Slf4j
public class RqueueMessageManagerImpl extends BaseMessageSender implements RqueueMessageManager {
  private RqueueMessageManagerImpl(
      RqueueMessageTemplate messageTemplate,
      List<MessageConverter> messageConverters,
      boolean addDefault) {
    super(messageTemplate);
    notEmpty(messageConverters, "messageConverters cannot be empty");
    init(messageConverters, addDefault);
  }

  public RqueueMessageManagerImpl(RqueueMessageTemplate messageTemplate) {
    this(messageTemplate, Collections.singletonList(new GenericMessageConverter()), false);
  }

  public RqueueMessageManagerImpl(
      RqueueMessageTemplate messageTemplate, List<MessageConverter> messageConverters) {
    this(messageTemplate, messageConverters, false);
  }

  @Override
  public boolean deleteAllMessages(String queueName) {
    QueueDetail queueDetail = EndpointRegistry.get(queueName);
    try {
      stringRqueueRedisTemplate.delete(queueDetail.getQueueName());
      stringRqueueRedisTemplate.delete(queueDetail.getProcessingQueueName());
      stringRqueueRedisTemplate.delete(queueDetail.getDelayedQueueName());
      return true;
    } catch (Exception e) {
      log.error("Delete all message failed", e);
      return false;
    }
  }

  @Override
  public List<Object> getAllMessages(String queueName) {
    List<Object> messages = new ArrayList<>();
    QueueDetail queueDetail = EndpointRegistry.get(queueName);
    for (RqueueMessage message :
        messageTemplate.getAllMessages(
            queueDetail.getQueueName(),
            queueDetail.getProcessingQueueName(),
            queueDetail.getDelayedQueueName())) {
      messages.add(MessageUtils.convertMessageToObject(message, messageConverter));
    }
    return messages;
  }

  @Override
  public Object getMessage(String queueName, String id) {
    RqueueMessage rqueueMessage = getRqueueMessage(queueName, id);
    if (rqueueMessage == null) {
      return null;
    }
    Message<String> message =
        MessageBuilder.createMessage(
            rqueueMessage.getMessage(), RqueueMessageHeaders.emptyMessageHeaders());
    return messageConverter.fromMessage(message, null);
  }

  @Override
  public RqueueMessage getRqueueMessage(String queueName, String id) {
    MessageMetadata messageMetadata = rqueueMessageMetadataService.getByMessageId(queueName, id);
    if (messageMetadata == null || messageMetadata.getRqueueMessage() == null) {
      return null;
    }
    if (messageMetadata.getStatus() == null) {
      return messageMetadata.getRqueueMessage();
    }
    if (messageMetadata.getStatus() == TaskStatus.PROCESSING
        || messageMetadata.getStatus() == TaskStatus.SUCCESSFUL) {
      return null;
    }
    return messageMetadata.getRqueueMessage();
  }

  @Override
  public boolean exist(String queueName, String id) {
    return getMessage(queueName, id) != null;
  }

  @Override
  public boolean deleteMessage(String queueName, String id) {
    RqueueMessage rqueueMessage = getRqueueMessage(queueName, id);
    if (rqueueMessage == null) {
      return false;
    }
    rqueueMessageMetadataService.deleteMessage(
        queueName, id, Duration.ofMinutes(rqueueConfig.getMessageDurabilityInMinute()));
    return true;
  }
}
