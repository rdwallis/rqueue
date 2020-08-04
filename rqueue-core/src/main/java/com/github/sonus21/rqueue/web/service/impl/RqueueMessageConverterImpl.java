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

package com.github.sonus21.rqueue.web.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sonus21.rqueue.converter.GenericMessageConverter.Msg;
import com.github.sonus21.rqueue.exception.ProcessingException;
import com.github.sonus21.rqueue.models.PubSubMessage;
import com.github.sonus21.rqueue.utils.SerializationUtils;
import com.github.sonus21.rqueue.web.service.RqueueMessageConverter;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.stereotype.Component;

@Component
public class RqueueMessageConverterImpl implements RqueueMessageConverter {
  private ObjectMapper objectMapper;

  @Autowired
  public RqueueMessageConverterImpl() {
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public byte[] toMessage(PubSubMessage message) throws ProcessingException {
    try {
      return objectMapper.writeValueAsBytes(message);
    } catch (JsonProcessingException e) {
      throw new ProcessingException(e);
    }
  }

  @Override
  public PubSubMessage fromMessage(Message message) throws ProcessingException {
    if (SerializationUtils.isEmpty(message.getBody())) {
      return null;
    }
    try {
      return objectMapper.readValue(message.getBody(), PubSubMessage.class);
    } catch (IOException e) {
      throw new ProcessingException(e);
    }
  }

  @Override
  public String fromMessage(com.github.sonus21.rqueue.models.request.Message message)
      throws ProcessingException {
    Msg msg = new Msg(message.getClazz(), message.getBody());
    try {
      return objectMapper.writeValueAsString(msg);
    } catch (JsonProcessingException e) {
      throw new ProcessingException(e);
    }
  }

  @Override
  public com.github.sonus21.rqueue.models.request.Message toMessage(String message)
      throws ProcessingException {
    Msg msg;
    try {
      msg = objectMapper.readValue(message, Msg.class);
    } catch (JsonProcessingException e) {
      throw new ProcessingException(e);
    }
    return new com.github.sonus21.rqueue.models.request.Message(msg.getName(), msg.getMsg());
  }
}
