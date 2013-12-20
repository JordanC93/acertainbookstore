package com.acertainbookstore.business;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import com.acertainbookstore.interfaces.Replicator;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreMessageTag;
import com.acertainbookstore.utils.BookStoreResult;
import com.acertainbookstore.utils.BookStoreUtility;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;

/**
 * CertainBookStoreReplicator is used to replicate updates to slaves
 * concurrently.
 */
public class CertainBookStoreReplicator implements Replicator {
    private ThreadPoolExecutor tpe;

	public CertainBookStoreReplicator(int maxReplicatorThreads) {
        tpe = new ThreadPoolExecutor(maxReplicatorThreads, maxReplicatorThreads, 2, TimeUnit.MINUTES,
                                     new LinkedBlockingQueue<Runnable>());
	}

	public List<Future<ReplicationResult>> replicate(Set<String> slaveServers,
			ReplicationRequest request) {
        List<Future<ReplicationResult>> results = new ArrayList<Future<ReplicationResult>>();
		for (String slaveServerAddress : slaveServers) {
            FutureTask<ReplicationResult> replicateTask = new FutureTask<ReplicationResult>(
                    new CertainBookStoreReplicationTask(slaveServerAddress, request)
            );
            tpe.execute(replicateTask);
            results.add(replicateTask);
        }
		return results;
	}

}
