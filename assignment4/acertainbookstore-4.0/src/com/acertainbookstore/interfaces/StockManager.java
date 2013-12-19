package com.acertainbookstore.interfaces;

import java.util.List;
import java.util.Set;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.BookEditorPick;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.utils.BookStoreException;

/**
 * StockManager declares a set of methods exposed by the proxies to the clients. These
 * methods need to be implemented by both the proxies and CertainBookStore.
 */
public interface StockManager {

	/**
	 * Adds the books in bookSet to the stock.
	 * 
	 * @param bookSet
	 * @return
	 * @throws BookStoreException
	 */
	public void addBooks(Set<StockBook> bookSet)
			throws BookStoreException;

	/**
	 * Add copies of the existing book to the bookstore.
	 * 
	 * @param ISBN
	 * @param noCopies
	 * @return
	 * @throws BookStoreException
	 */
	public void addCopies(Set<BookCopy> bookCopiesSet)
			throws BookStoreException;


	/**
	 * Books are marked/unmarked as an editor pick
	 * 
	 * @return
	 * @throws BookStoreException
	 */
	public void updateEditorPicks(Set<BookEditorPick> editorPicks) throws BookStoreException;
	
	/**
	 * Returns the list of books in the bookstore
	 * 
	 * @return
	 * @throws BookStoreException
	 */
	public List<StockBook> getBooks() throws BookStoreException;

	/**
	 * Returns the list of books which has sale miss
	 * 
	 * @return
	 * @throws BookStoreException
	 */
	public List<StockBook> getBooksInDemand() throws BookStoreException;

}