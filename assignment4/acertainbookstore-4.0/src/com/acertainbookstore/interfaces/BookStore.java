/**
 * 
 */
package com.acertainbookstore.interfaces;

import java.util.List;
import java.util.Set;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.BookRating;
import com.acertainbookstore.utils.BookStoreException;

/**
 * BookStore declares a set of methods exposed by the proxies to the clients. These
 * methods need to be implemented by both the proxies and CertainBookStore. 
 */
public interface BookStore {

	/**
	 * Buy the sets of books specified.
	 * @param ISBNList
	 * @throws BookStoreException
	 */
	public void buyBooks(Set<BookCopy> booksToBuy) throws BookStoreException;

	/**
	 * Applies the BookRatings in the set, i.e. rates each book with their
	 * respective rating.
	 * 
	 * @param bookRating
	 * @return
	 * @throws BookStoreException
	 */
	public void rateBooks(Set<BookRating> bookRating)
			throws BookStoreException;

	/**
	 * Returns the list of books corresponding to the set of ISBNs
	 * 
	 * @param ISBNList
	 * @return
	 * @throws BookStoreException
	 */
	public List<Book> getBooks(Set<Integer> ISBNList)
			throws BookStoreException;

	/**
	 * Return a list of top rated numBooks
	 * books.
	 * 
	 * @param numBooks
	 * @return
	 * @throws BookStoreException
	 */
	public List<Book> getTopRatedBooks(int numBooks) throws BookStoreException;

	/**
	 * Returns the list of books containing numBooks editor picks
	 * 
	 * @param numBooks
	 * @return
	 * @throws BookStoreException
	 */
	public List<Book> getEditorPicks(int numBooks) throws BookStoreException;

}
