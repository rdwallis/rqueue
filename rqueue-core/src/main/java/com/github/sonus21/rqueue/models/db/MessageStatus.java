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

package com.github.sonus21.rqueue.models.db;

import com.github.sonus21.rqueue.models.enums.TaskStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum MessageStatus {
  // Message is just enqueued
  ENQUEUED(false, null),
  // Currently this message is being processed
  PROCESSING(false, null),
  // Message was deleted
  DELETED(true, TaskStatus.DELETED),
  // Message was ignored by pre processor
  IGNORED(true, TaskStatus.IGNORED),
  // Message was successful consumed
  SUCCESSFUL(true, TaskStatus.SUCCESSFUL),
  // Message moved to dead letter queue
  MOVED_TO_DLQ(true, TaskStatus.MOVED_TO_DLQ),
  /**
   * Message was discarded due to retry limit or {@link
   * com.github.sonus21.rqueue.utils.backoff.TaskExecutionBackOff#STOP} was returned by task
   * execution backoff method.
   */
  DISCARDED(true, TaskStatus.DISCARDED),
  // Execution has failed, it will retried later
  FAILED(false, null);
  private final boolean terminalState;
  private final TaskStatus taskStatus;
}
