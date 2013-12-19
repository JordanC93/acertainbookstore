package com.acertainbookstore.business;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import com.acertainbookstore.interfaces.Replicator;

/**
 * CertainBookStoreReplicator is used to replicate updates to slaves
 * concurrently.
 */
public class CertainBookStoreReplicator implements Replicator {

	public CertainBookStoreReplicator(int maxReplicatorThreads) {
		// TODO:Implement this constructor
	}

	public List<Future<ReplicationResult>> replicate(Set<String> slaveServers,
			ReplicationRequest request) {
		// TODO: Implement this method
		return null;
	}

}
