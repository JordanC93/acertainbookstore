package com.acertainbookstore.business;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.acertainbookstore.interfaces.ReplicatedBookStore;
import com.acertainbookstore.interfaces.ReplicatedStockManager;
import com.acertainbookstore.interfaces.Replicator;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreMessageTag;
import com.acertainbookstore.utils.BookStoreResult;

/**
 * MasterCertainBookStore is a wrapper over the CertainBookStore class and
 * supports the ReplicatedBookStore and ReplicatedStockManager interfaces
 * 
 * This class also contains a Replicator which replicates updates to slaves.
 * 
 * Designed using the singleton design pattern.
 * 
 */
public class MasterCertainBookStore implements ReplicatedBookStore,
		ReplicatedStockManager {
	private CertainBookStore bookStore = null;
	private static MasterCertainBookStore instance = null;
	private long snapShotId = 0;
	private Replicator replicator = null;
	private Set<String> slaveServers;
	private static int maxReplicatorThreads = 10;
	private static String filePath = "/universe/pcsd/acertainbookstore/src/server.properties";

	private MasterCertainBookStore() throws Exception {
		bookStore = CertainBookStore.getInstance();
		replicator = new CertainBookStoreReplicator(maxReplicatorThreads);
		initializeSlaveMapping();
	}

	private void initializeSlaveMapping() throws Exception {
		Properties props = new Properties();
		slaveServers = new HashSet<String>();

		props.load(new FileInputStream(filePath));

		String slaveAddresses = props
				.getProperty(BookStoreConstants.KEY_SLAVE);
		for (String slave : slaveAddresses
				.split(BookStoreConstants.SPLIT_SLAVE_REGEX)) {
			if (!slave.toLowerCase().startsWith("http://")) {
				slave = new String("http://" + slave);
			}
			if (!slave.endsWith("/")) {
				slave = new String(slave + "/");
			}

			this.slaveServers.add(slave);
		}
	}

	public synchronized static MasterCertainBookStore getInstance()
			throws BookStoreException {
		if (instance == null) {

			try {
				instance = new MasterCertainBookStore();
			} catch (Exception ex) {
				throw new BookStoreException(ex);
			}
		}
		return instance;
	}

	private void waitForSlaveUpdates(
			List<Future<ReplicationResult>> replicatedSlaveFutures) {
		List<ReplicationResult> slaveServers = new ArrayList<ReplicationResult>();
		for (Future<ReplicationResult> slaveServer : replicatedSlaveFutures) {
			try {
				// block until the future result is available
				slaveServers.add(slaveServer.get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

		for (ReplicationResult slaveServer : slaveServers) {
			if (!slaveServer.isReplicationSuccessful()) {
				// Remove the server from the list of servers - fail stop model
				this.slaveServers.remove(slaveServer.getServerAddress());
			}
		}
	}

	public synchronized BookStoreResult getBooks(Set<Integer> ISBNList)
			throws BookStoreException {
		BookStoreResult result = new BookStoreResult(
				bookStore.getBooks(ISBNList), snapShotId);
		return result;
	}

	public synchronized BookStoreResult getTopRatedBooks(int numBooks)
			throws BookStoreException {
		throw new BookStoreException();
	}

	public synchronized BookStoreResult getEditorPicks(int numBooks)
			throws BookStoreException {
		BookStoreResult result = new BookStoreResult(
				bookStore.getEditorPicks(numBooks), snapShotId);
		return result;
	}

	public synchronized BookStoreResult getBooks() throws BookStoreException {
		BookStoreResult result = new BookStoreResult(bookStore.getBooks(),
				snapShotId);
		return result;
	}

	public synchronized BookStoreResult getBooksInDemand()
			throws BookStoreException {
		throw new BookStoreException();
	}

	public synchronized BookStoreResult addBooks(Set<StockBook> bookSet)
			throws BookStoreException {

		ReplicationRequest request = new ReplicationRequest(bookSet,
				BookStoreMessageTag.ADDBOOKS);
		List<Future<ReplicationResult>> replicatedSlaveFutures = replicator
				.replicate(slaveServers, request);
		bookStore.addBooks(bookSet); // If this fails it will throw an exception
		snapShotId++;
		waitForSlaveUpdates(replicatedSlaveFutures);
		BookStoreResult result = new BookStoreResult(null, snapShotId);
		return result;
	}

	public synchronized BookStoreResult addCopies(Set<BookCopy> bookCopiesSet)
			throws BookStoreException {
		ReplicationRequest request = new ReplicationRequest(bookCopiesSet,
				BookStoreMessageTag.ADDCOPIES);
		List<Future<ReplicationResult>> replicatedSlaveFutures = replicator
				.replicate(slaveServers, request);
		bookStore.addCopies(bookCopiesSet); // If this fails it will throw an
											// exception
		snapShotId++;
		waitForSlaveUpdates(replicatedSlaveFutures);
		BookStoreResult result = new BookStoreResult(null, snapShotId);
		return result;
	}

	public synchronized BookStoreResult updateEditorPicks(
			Set<BookEditorPick> editorPicks) throws BookStoreException {
		ReplicationRequest request = new ReplicationRequest(editorPicks,
				BookStoreMessageTag.UPDATEEDITORPICKS);
		List<Future<ReplicationResult>> replicatedSlaveFutures = replicator
				.replicate(slaveServers, request);
		bookStore.updateEditorPicks(editorPicks); // If this fails it will throw
													// an exception
		snapShotId++;
		waitForSlaveUpdates(replicatedSlaveFutures);
		BookStoreResult result = new BookStoreResult(null, snapShotId);
		return result;
	}

	public synchronized BookStoreResult buyBooks(Set<BookCopy> booksToBuy)
			throws BookStoreException {
		ReplicationRequest request = new ReplicationRequest(booksToBuy,
				BookStoreMessageTag.BUYBOOKS);
		List<Future<ReplicationResult>> replicatedSlaveFutures = replicator
				.replicate(slaveServers, request);
		bookStore.buyBooks(booksToBuy); // If this fails it will throw an
										// exception
		snapShotId++;
		waitForSlaveUpdates(replicatedSlaveFutures);
		BookStoreResult result = new BookStoreResult(null, snapShotId);
		return result;
	}

	public synchronized BookStoreResult rateBooks(Set<BookRating> bookRating)
			throws BookStoreException {

		throw new BookStoreException();
	}

}
