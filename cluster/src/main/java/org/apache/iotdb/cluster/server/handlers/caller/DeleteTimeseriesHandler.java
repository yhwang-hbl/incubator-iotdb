/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.iotdb.cluster.server.handlers.caller;

import org.apache.iotdb.cluster.rpc.thrift.DeleteTimeseriesRespPair;
import org.apache.iotdb.cluster.rpc.thrift.Node;
import org.apache.thrift.async.AsyncMethodCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

public class DeleteTimeseriesHandler implements AsyncMethodCallback<DeleteTimeseriesRespPair> {
  private static final Logger logger = LoggerFactory.getLogger(DeleteTimeseriesHandler.class);

  private Node contact;
  private AtomicReference<DeleteTimeseriesRespPair> result;

  @Override
  public void onComplete(DeleteTimeseriesRespPair resp) {
    logger.info("Received delete timeseries result from {}", contact);
    synchronized (result) {
      result.set(resp);
      result.notifyAll();
    }
  }

  @Override
  public void onError(Exception exception) {
    logger.warn("Cannot delete timeseries from {}, because ", contact, exception);
    synchronized (result) {
      result.notifyAll();
    }
  }

  public void setResponse(AtomicReference response) {
    this.result = response;
  }

  public void setContact(Node contact) {
    this.contact = contact;
  }
}