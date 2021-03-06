package com.acertainbookstore.interfaces;

import java.util.Set;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.BookEditorPick;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreResult;

/**
 *
 * ReplicatedStockManager declares a set of methods conforming to the StockManager
 * interface exposed by the bookstore to the proxies. These methods need to be
 * implemented by MasterCertainBookStore.
 *
 */
public interface ReplicatedStockManager extends ReplicatedReadOnlyStockManager {

	/**
	 * Adds the books in bookSet to the stock.
	 * 
	 * @param bookSet
	 * @return
	 * @throws BookStoreException
	 */
	public BookStoreResult addBooks(Set<StockBook> bookSet)
			throws BookStoreException;

	/**
	 * Add copies of the existing book to the bookstore.
	 * 
	 * @param ISBN
	 * @param noCopies
	 * @return
	 * @throws BookStoreException
	 */
	public BookStoreResult addCopies(Set<BookCopy> bookCopiesSet)
			throws BookStoreException;


	/**
	 * Books are marked/unmarked as an editor pick
	 * 
	 * @return
	 * @throws BookStoreException
	 */
	public BookStoreResult updateEditorPicks(Set<BookEditorPick> editorPicks) throws BookStoreException;

}
