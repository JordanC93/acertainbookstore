package com.acertainbookstore.interfaces;

import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreResult;

/**
 * ReplicatedReadOnlyStockManager declares a set of read only methods conforming
 * to StockManager interface exposed by the bookstore to the proxies. These
 * methods need to be implemented by SlaveCertainBookStore.
 */
public interface ReplicatedReadOnlyStockManager {

	/**
	 * Returns the list of books in the bookstore
	 * 
	 * @return
	 * @throws BookStoreException
	 */
	public BookStoreResult getBooks() throws BookStoreException;

	/**
	 * Returns the list of books which has sale miss
	 * 
	 * @return
	 * @throws BookStoreException
	 */
	public BookStoreResult getBooksInDemand() throws BookStoreException;

}
