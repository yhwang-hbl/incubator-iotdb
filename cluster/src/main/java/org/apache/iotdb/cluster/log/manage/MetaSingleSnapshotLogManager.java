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

package org.apache.iotdb.cluster.log.manage;

import java.util.Map;
import org.apache.iotdb.cluster.log.LogApplier;
import org.apache.iotdb.cluster.log.Snapshot;
import org.apache.iotdb.cluster.log.manage.serializable.SyncLogDequeSerializer;
import org.apache.iotdb.cluster.log.snapshot.MetaSimpleSnapshot;
import org.apache.iotdb.cluster.server.member.MetaGroupMember;
import org.apache.iotdb.db.auth.AuthException;
import org.apache.iotdb.db.auth.authorizer.BasicAuthorizer;
import org.apache.iotdb.db.auth.authorizer.IAuthorizer;
import org.apache.iotdb.db.auth.entity.Role;
import org.apache.iotdb.db.auth.entity.User;
import org.apache.iotdb.db.service.IoTDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MetaSingleSnapshotLogManager provides a MetaSimpleSnapshot as snapshot.
 */
public class MetaSingleSnapshotLogManager extends RaftLogManager {

  private static final Logger logger = LoggerFactory.getLogger(MetaSingleSnapshotLogManager.class);
  private Map<String, Long> storageGroupTTLMap;
  private Map<String, User> userMap;
  private Map<String, Role> roleMap;
  private MetaGroupMember metaGroupMember;

  public MetaSingleSnapshotLogManager(LogApplier logApplier, MetaGroupMember metaGroupMember) {
    super(new SyncLogDequeSerializer(0), logApplier, metaGroupMember.getName());
    this.metaGroupMember = metaGroupMember;
  }

  @Override
  public void takeSnapshot() {
    storageGroupTTLMap = IoTDB.metaManager.getStorageGroupsTTL();
    try {
      IAuthorizer authorizer = BasicAuthorizer.getInstance();
      userMap = authorizer.getAllUsers();
      roleMap = authorizer.getAllRoles();

    } catch (AuthException e) {
      logger.error("get user or role info failed", e);
    }
  }

  @Override
  public Snapshot getSnapshot() {
    takeSnapshot();
    MetaSimpleSnapshot snapshot = new MetaSimpleSnapshot(storageGroupTTLMap, userMap, roleMap,
        metaGroupMember.getPartitionTable().serialize());

    snapshot.setLastLogIndex(getCommitLogIndex());
    snapshot.setLastLogTerm(getCommitLogTerm());
    return snapshot;
  }
}