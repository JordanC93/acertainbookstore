/**
 * 
 */
package com.acertainbookstore.server;


/**
 * Starts the book store HTTP server that the clients will communicate with.
 */
public class BookStoreHTTPServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BookStoreHTTPMessageHandler handler = new BookStoreHTTPMessageHandler();
		if (BookStoreHTTPServerUtility.createServer(8081, handler)) {
			;
		}
	}

}
