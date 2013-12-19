 /**
 * 
 */
package com.acertainbookstore.server;


/**
 * Starts the master bookstore HTTP server.
 */
public class MasterBookStoreHTTPServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MasterBookStoreHTTPMessageHandler handler = new MasterBookStoreHTTPMessageHandler();
		if (BookStoreHTTPServerUtility.createServer(8081, handler)) {
			;
		}
	}

}
