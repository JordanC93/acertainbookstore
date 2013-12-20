/**
 * 
 */
package com.acertainbookstore.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.SynchronousQueue;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.BookRating;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreMessageTag;
import com.acertainbookstore.utils.BookStoreResult;
import com.acertainbookstore.utils.BookStoreUtility;

/**
 * 
 * ReplicationAwareBookStoreHTTPProxy implements the client level synchronous
 * CertainBookStore API declared in the BookStore class. It keeps retrying the
 * API until a consistent reply is returned from the replicas
 * 
 */
public class ReplicationAwareBookStoreHTTPProxy implements BookStore {
	private HttpClient client;
	private Set<String> slaveAddresses;
	private String masterAddress;
	private String filePath = "/universe/pcsd/acertainbookstore/src/proxy.properties";
	private volatile long snapshotId = 0;
    // For load balancing:
    private volatile Queue<String> pendingSlaves;
    private volatile int slaveHits, masterHits;

	public long getSnapshotId() {
		return snapshotId;
	}

	public void setSnapshotId(long snapShotId) {
		this.snapshotId = snapShotId;
	}

	/**
	 * Initialize the client object
	 */
	public ReplicationAwareBookStoreHTTPProxy() throws Exception {
		initializeReplicationAwareMappings();
		client = new HttpClient();
		client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
		client.setMaxConnectionsPerAddress(BookStoreClientConstants.CLIENT_MAX_CONNECTION_ADDRESS); // max
																									// concurrent
																									// connections
																									// to
																									// every
																									// address
		client.setThreadPool(new QueuedThreadPool(
				BookStoreClientConstants.CLIENT_MAX_THREADSPOOL_THREADS)); // max
																			// threads
		client.setTimeout(BookStoreClientConstants.CLIENT_MAX_TIMEOUT_MILLISECS); // seconds
																					// timeout;
																					// if
																					// no
																					// server
																					// reply,
																					// the
																					// request
																					// expires
		client.start();
	}

	private void initializeReplicationAwareMappings() throws IOException {

		Properties props = new Properties();
		slaveAddresses = new HashSet<String>();

		props.load(new FileInputStream(filePath));
		this.masterAddress = props
				.getProperty(BookStoreConstants.KEY_MASTER);
		if (!this.masterAddress.toLowerCase().startsWith("http://")) {
			this.masterAddress = new String("http://" + this.masterAddress);
		}

		String slaveAddresses = props
				.getProperty(BookStoreConstants.KEY_SLAVE);
		for (String slave : slaveAddresses
				.split(BookStoreConstants.SPLIT_SLAVE_REGEX)) {
			if (!slave.toLowerCase().startsWith("http://")) {
				slave = new String("http://" + slave);
			}
			this.slaveAddresses.add(slave);
		}

        slaveHits = masterHits = 0;
        pendingSlaves = new SynchronousQueue<String>();
	}

	public synchronized String getReplicaAddress() {
        if (pendingSlaves.isEmpty()) {
            if (slaveHits > masterHits) {
                masterHits ++;
                return this.masterAddress;
            } else {
                slaveHits ++;
                List<String> slavesToAdd = new ArrayList<String>(this.slaveAddresses);
                Collections.shuffle(slavesToAdd);
                pendingSlaves.addAll(slavesToAdd);
            }
        }

        return pendingSlaves.poll();
	}

	public String getMasterServerAddress() {
        masterHits ++;
		return this.masterAddress;
	}

	public void buyBooks(Set<BookCopy> isbnSet) throws BookStoreException {

		String listISBNsxmlString = BookStoreUtility
				.serializeObjectToXMLString(isbnSet);
		Buffer requestContent = new ByteArrayBuffer(listISBNsxmlString);

		BookStoreResult result = null;

		ContentExchange exchange = new ContentExchange();
		String urlString = getMasterServerAddress() + "/"
				+ BookStoreMessageTag.BUYBOOKS;
		exchange.setMethod("POST");
		exchange.setURL(urlString);
		exchange.setRequestContent(requestContent);
		result = BookStoreUtility.SendAndRecv(this.client, exchange);
		this.setSnapshotId(result.getSnapshotId());
	}

	@SuppressWarnings("unchecked")
	public List<Book> getBooks(Set<Integer> isbnSet) throws BookStoreException {

		String listISBNsxmlString = BookStoreUtility
				.serializeObjectToXMLString(isbnSet);
		Buffer requestContent = new ByteArrayBuffer(listISBNsxmlString);

		BookStoreResult result = null;
		do {
			ContentExchange exchange = new ContentExchange();
			String urlString = getReplicaAddress() + "/"
					+ BookStoreMessageTag.GETBOOKS;
			exchange.setMethod("POST");
			exchange.setURL(urlString);
			exchange.setRequestContent(requestContent);
			result = BookStoreUtility.SendAndRecv(this.client, exchange);
		} while (result.getSnapshotId() < this.getSnapshotId());
		this.setSnapshotId(result.getSnapshotId());
		return (List<Book>) result.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Book> getEditorPicks(int numBooks) throws BookStoreException {
		ContentExchange exchange = new ContentExchange();
		String urlEncodedNumBooks = null;

		try {
			urlEncodedNumBooks = URLEncoder.encode(Integer.toString(numBooks),
					"UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new BookStoreException("unsupported encoding of numbooks", ex);
		}

		BookStoreResult result = null;
		do {
			String urlString = getReplicaAddress() + "/"
					+ BookStoreMessageTag.EDITORPICKS + "?"
					+ BookStoreConstants.BOOK_NUM_PARAM + "="
					+ urlEncodedNumBooks;
			exchange.setURL(urlString);
			result = BookStoreUtility.SendAndRecv(this.client, exchange);
		} while (result.getSnapshotId() < this.getSnapshotId());
		this.setSnapshotId(result.getSnapshotId());

		return (List<Book>) result.getResultList();
	}

	public void stop() {
		try {
			client.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void rateBooks(Set<BookRating> bookRating) throws BookStoreException {
		// TODO Auto-generated method stub
		throw new BookStoreException();

	}

	@Override
	public List<Book> getTopRatedBooks(int numBooks) throws BookStoreException {
		// TODO Auto-generated method stub
		throw new BookStoreException();
	}

}
