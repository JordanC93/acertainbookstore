package com.acertainbookstore.business;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreUtility;

public class ConcurrentCertainBookStore implements BookStore, StockManager{
	private static ConcurrentCertainBookStore singleInstance;
	private static Map<Integer, BookStoreBook> bookMap;
    private static ReadWriteLock bookMapLock;
	
	private ConcurrentCertainBookStore() {
		// TODO Auto-generated constructor stub
	}

	public synchronized static ConcurrentCertainBookStore getInstance() {
		if (singleInstance != null) {
			return singleInstance;
		} else {
			singleInstance = new ConcurrentCertainBookStore();
			bookMap = new HashMap<Integer, BookStoreBook>();
            bookMapLock = new ReentrantReadWriteLock();
		}
		return singleInstance;
	}

	public void addBooks(Set<StockBook> bookSet)
			throws BookStoreException {
		if (bookSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}
		// Check if all are there
		for (StockBook book : bookSet) {
			int ISBN = book.getISBN();
			String bookTitle = book.getTitle();
			String bookAuthor = book.getAuthor();
			int noCopies = book.getNumCopies();
			float bookPrice = book.getPrice();
			if (BookStoreUtility.isInvalidISBN(ISBN)
					|| BookStoreUtility.isEmpty(bookTitle)
					|| BookStoreUtility.isEmpty(bookAuthor)
					|| BookStoreUtility.isInvalidNoCopies(noCopies)
					|| bookPrice < 0.0) {
				throw new BookStoreException(BookStoreConstants.BOOK
						+ book.toString() + BookStoreConstants.INVALID);
			} else if (bookMap.containsKey(ISBN)) {
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN
						+ BookStoreConstants.DUPLICATED);
			}
		}

        Lock l = bookMapLock.writeLock();
        try {
            l.lock();
            for (StockBook book : bookSet) {
                int ISBN = book.getISBN();
                bookMap.put(ISBN, new BookStoreBook(book));
            }
        } finally {
            l.unlock();
        }
		return;
	}

	public void addCopies(Set<BookCopy> bookCopiesSet)
			throws BookStoreException {
		int ISBN, numCopies;

		if (bookCopiesSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

        Lock l = bookMapLock.writeLock();
        try {
            l.lock();
            for (BookCopy bookCopy : bookCopiesSet) {
                ISBN = bookCopy.getISBN();
                numCopies = bookCopy.getNumCopies();
                if (BookStoreUtility.isInvalidISBN(ISBN))
                    throw new BookStoreException(BookStoreConstants.ISBN + ISBN
                            + BookStoreConstants.INVALID);
                if (!bookMap.containsKey(ISBN))
                    throw new BookStoreException(BookStoreConstants.ISBN + ISBN
                            + BookStoreConstants.INVALID);
                if (BookStoreUtility.isInvalidNoCopies(numCopies))
                    throw new BookStoreException(BookStoreConstants.NUM_COPIES
                            + numCopies + BookStoreConstants.INVALID);

            }

            BookStoreBook book;
            // Update the number of copies
            for (BookCopy bookCopy : bookCopiesSet) {
                ISBN = bookCopy.getISBN();
                numCopies = bookCopy.getNumCopies();
                book = bookMap.get(ISBN);
                book.addCopies(numCopies);
            }
        } finally {
            l.unlock();
        }
	}

	public List<StockBook> getBooks() {
        List<StockBook> listBooks = new ArrayList<StockBook>();

        Lock l = bookMapLock.readLock();
        try {
            l.lock();
            Collection<BookStoreBook> bookMapValues = bookMap.values();
            for (BookStoreBook book : bookMapValues) {
                listBooks.add(book.immutableStockBook());
            }
        } finally {
            l.unlock();
        }
		return listBooks;
	}

	public void updateEditorPicks(Set<BookEditorPick> editorPicks)
			throws BookStoreException {
		// Check that all ISBNs that we add/remove are there first.
		if (editorPicks == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		int ISBNVal;

        Lock l = bookMapLock.writeLock();
        try {
            l.lock();
            for (BookEditorPick editorPickArg : editorPicks) {
                ISBNVal = editorPickArg.getISBN();
                if (BookStoreUtility.isInvalidISBN(ISBNVal))
                    throw new BookStoreException(BookStoreConstants.ISBN + ISBNVal
                            + BookStoreConstants.INVALID);
                if (!bookMap.containsKey(ISBNVal))
                    throw new BookStoreException(BookStoreConstants.ISBN + ISBNVal
                            + BookStoreConstants.NOT_AVAILABLE);
            }

            for (BookEditorPick editorPickArg : editorPicks) {
                bookMap.get(editorPickArg.getISBN()).setEditorPick(
                        editorPickArg.isEditorPick());
            }
        } finally {
            l.unlock();
        }
		return;
	}

	public void buyBooks(Set<BookCopy> bookCopiesToBuy)
			throws BookStoreException {
		if (bookCopiesToBuy == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}
		// Check that all ISBNs that we buy are there first.
		int ISBN;
		BookStoreBook book;
		Boolean saleMiss = false;
        Lock l = bookMapLock.writeLock();
        try {
            l.lock();
            for (BookCopy bookCopyToBuy : bookCopiesToBuy) {
                ISBN = bookCopyToBuy.getISBN();
                if (BookStoreUtility.isInvalidISBN(ISBN))
                    throw new BookStoreException(BookStoreConstants.ISBN + ISBN
                            + BookStoreConstants.INVALID);
                if (!bookMap.containsKey(ISBN))
                    throw new BookStoreException(BookStoreConstants.ISBN + ISBN
                            + BookStoreConstants.NOT_AVAILABLE);
                book = bookMap.get(ISBN);
                if (!book.areCopiesInStore(bookCopyToBuy.getNumCopies())) {
                    book.addSaleMiss(); // If we cannot sell the copies of the book
                                        // its a miss
                    saleMiss = true;
                }
            }

            // We throw exception now since we want to see how many books in the
            // order incurred misses which is used by books in demand
            if (saleMiss)
                throw new BookStoreException(BookStoreConstants.BOOK
                        + BookStoreConstants.NOT_AVAILABLE);

            // Then make purchase
            for (BookCopy bookCopyToBuy : bookCopiesToBuy) {
                book = bookMap.get(bookCopyToBuy.getISBN());
                book.buyCopies(bookCopyToBuy.getNumCopies());
            }
        } finally {
            l.unlock();
        }
		return;
	}

	public List<Book> getBooks(Set<Integer> isbnSet)
			throws BookStoreException {
		if (isbnSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

        List<Book> listBooks = new ArrayList<Book>();
        Lock l = bookMapLock.readLock();
        try {
            l.lock();
            // Check that all ISBNs that we rate are there first.
            for (Integer ISBN : isbnSet) {
                if (BookStoreUtility.isInvalidISBN(ISBN))
                    throw new BookStoreException(BookStoreConstants.ISBN + ISBN
                            + BookStoreConstants.INVALID);
                if (!bookMap.containsKey(ISBN))
                    throw new BookStoreException(BookStoreConstants.ISBN + ISBN
                            + BookStoreConstants.NOT_AVAILABLE);
            }

            // Get the books
            for (Integer ISBN : isbnSet) {
                listBooks.add(bookMap.get(ISBN).immutableBook());
            }
        } finally {
            l.unlock();
        }
		return listBooks;
	}


	public List<Book> getEditorPicks(int numBooks)
			throws BookStoreException {
		if (numBooks < 0) {
			throw new BookStoreException("numBooks = " + numBooks
					+ ", but it must be positive");
		}

        BookStoreBook book;
        List<BookStoreBook> listAllEditorPicks = new ArrayList<BookStoreBook>();
        List<Book> listEditorPicks = new ArrayList<Book>();
        Lock l = bookMapLock.readLock();
        try {
            l.lock();
            Iterator<Entry<Integer, BookStoreBook>> it = bookMap.entrySet()
                    .iterator();

            // Get all books that are editor picks
            while (it.hasNext()) {
                Entry<Integer, BookStoreBook> pair = (Entry<Integer, BookStoreBook>) it
                        .next();
                book = (BookStoreBook) pair.getValue();
                if (book.isEditorPick()) {
                    listAllEditorPicks.add(book);
                }
            }
        } finally {
            l.unlock();
        }

		// Find numBooks random indices of books that will be picked
		Random rand = new Random();
		Set<Integer> tobePicked = new HashSet<Integer>();
		int rangePicks = listAllEditorPicks.size();
		if (rangePicks < numBooks) {
			throw new BookStoreException("Only " + rangePicks
					+ " editor picks are available.");
		}
		int randNum;
		while (tobePicked.size() < numBooks) {
			randNum = rand.nextInt(rangePicks);
			tobePicked.add(randNum);
		}

		// Get the numBooks random books
		for (Integer index : tobePicked) {
			book = listAllEditorPicks.get(index);
			listEditorPicks.add(book.immutableBook());
		}
		return listEditorPicks;

	}

	@Override
	public List<Book> getTopRatedBooks(int numBooks) throws BookStoreException {
		// TODO Auto-generated method stub
		throw new BookStoreException();
	}

	@Override
	public List<StockBook> getBooksInDemand() throws BookStoreException {
		// TODO Auto-generated method stub
		throw new BookStoreException();
	}

	@Override
	public void rateBooks(Set<BookRating> bookRating) throws BookStoreException {
		// TODO Auto-generated method stub
		throw new BookStoreException();
	}

	

}
