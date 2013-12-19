 /**
 * 
 */
package com.acertainbookstore.server;


/**
 * Starts the slave bookstore HTTP server.
 */
public class SlaveBookStoreHTTPServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SlaveBookStoreHTTPMessageHandler handler = new SlaveBookStoreHTTPMessageHandler();
		if (BookStoreHTTPServerUtility.createServer(8082, handler)) {
			;
		}
	}

}
