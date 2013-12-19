package com.acertainbookstore.interfaces;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import com.acertainbookstore.business.ReplicationRequest;
import com.acertainbookstore.business.ReplicationResult;

/**
 * 
 * Replicator is used to replicate updates on master to slaves
 * 
 */
public interface Replicator {

	/**
	 * Replicates the ReplicationRequest to the list of slave servers and
	 * returns the a Future object containing the status of replication to the
	 * slave servers
	 * 
	 * @param slaveServers
	 * @param request
	 * @return
	 */
	public List<Future<ReplicationResult>> replicate(Set<String> slaveServers,
			ReplicationRequest request);
}
