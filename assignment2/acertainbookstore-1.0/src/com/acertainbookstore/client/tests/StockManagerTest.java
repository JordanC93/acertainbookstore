package com.acertainbookstore.client.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.BookEditorPick;
import com.acertainbookstore.business.CertainBookStore;
import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.client.BookStoreHTTPProxy;
import com.acertainbookstore.client.StockManagerHTTPProxy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreException;

/**
 * Test class to test the StockManager interface
 *
 */
public class StockManagerTest {
	private static boolean localTest = true;
	private static StockManager storeManager;
	private static BookStore client;

	@BeforeClass
	public static void setUpBeforeClass() {
		try {
			if (localTest) {
				storeManager = CertainBookStore.getInstance();
				client = CertainBookStore.getInstance();
			} else {
				storeManager = new StockManagerHTTPProxy(
						"http://localhost:8081/stock");
				client = new BookStoreHTTPProxy("http://localhost:8081");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Here we want to test addBook functionality.
	 * 
	 * 1. We create a BookStoreBook instance and add it to a list. Please note
	 * that we are also checking that the title and the author name could be any
	 * valid arbitrary string, which could also contain special characters.
	 * 
	 * 2. We execute addBooks with the list creates in step 1 as a parameter.
	 * 
	 * 3. Now we execute storeManager.getBooks, which returns a list of all the
	 * books available.
	 * 
	 * 4. Finally we check whether the book added in step 2 is returned by
	 * getBooks. 5. We also try to add invalid books and check that they throw
	 * appropriate exceptions.
	 */
	@Test
	public void testAddBook() {
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		Integer testISBN = 100;
		booksToAdd.add(new ImmutableStockBook(testISBN,
				"*@#&)%rdrrdjtIHH%^#)$&N 37874\n \t",
				"eurgqo	89274267^&#@&$%@&%$( \t", (float) 100, 5, 0, 0, 0,
				false));

		List<StockBook> listBooks = null;
		try {
			storeManager.addBooks(booksToAdd);
			listBooks = storeManager.getBooks();
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}

		Boolean containsTestISBN = false;
		Iterator<StockBook> it = listBooks.iterator();
		while (it.hasNext()) {
			Book b = it.next();
			if (b.getISBN() == testISBN)
				containsTestISBN = true;
		}
		assertTrue("List should contain the book added!", containsTestISBN);

		Boolean exceptionThrown = false;
		booksToAdd.add(new ImmutableStockBook(-1, "BookName", "Author",
				(float) 100, 5, 0, 0, 0, false));

		try {
			storeManager.addBooks(booksToAdd);
		} catch (BookStoreException e) {
			exceptionThrown = true;
		}
		assertTrue("Invlaid ISBN exception should be thrown!", exceptionThrown);
		List<StockBook> currentList = null;
		try {
			currentList = storeManager.getBooks();
			assertTrue(currentList.equals(listBooks));
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}

		exceptionThrown = false;
		booksToAdd.add(new ImmutableStockBook(testISBN + 1, "BookName",
				"Author", (float) 100, 0, 0, 0, 0, false));

		try {
			storeManager.addBooks(booksToAdd);
		} catch (BookStoreException e) {
			exceptionThrown = true;
		}
		assertTrue("Invalid number of copies exception should be thrown!",
				exceptionThrown);
		try {
			currentList = storeManager.getBooks();
			assertTrue(currentList.equals(listBooks));
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}

		exceptionThrown = false;
		booksToAdd.add(new ImmutableStockBook(testISBN + 2, "BookName",
				"Author", (float) -100, 0, 0, 0, 0, false));

		try {
			storeManager.addBooks(booksToAdd);
		} catch (BookStoreException e) {
			exceptionThrown = true;
		}
		assertTrue("Invlaid price exception should be thrown!", exceptionThrown);
		try {
			currentList = storeManager.getBooks();
			assertTrue(currentList.equals(listBooks));
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}

	}

	/**
	 * Here we want to test addCopies functionality. 1. We create a
	 * BookStoreBook instance with number of copies = 5.
	 * 
	 * 2. We execute addBooks with the list creates in step 1 as a parameter.
	 * 
	 * 3. Using storeManager.addCopies we add 2 more copies to the book added in
	 * step 2.
	 * 
	 * 4. We execute storeManager.getBooks and check the number of copies for
	 * the book added in step 2.
	 * 
	 * 5. Finally we test that the final number of copies = 7 (5 + 2).
	 * 
	 * 6. We also test that if we try to add invalid number of copies
	 * /non-existing ISBN/invalid ISBN BookStoreException is thrown.
	 * 
	 * 7. Finally we also test that all the invalid addCopies have not changed
	 * the initial status.
	 */
	@Test
	public void testAddCopies() {
		Integer testISBN = 200;
		Integer totalNumCopies = 7;

		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(new ImmutableStockBook(testISBN, "Book Name",
				"Book Author", (float) 100, 5, 0, 0, 0, false));
		try {
			storeManager.addBooks(booksToAdd);
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}

		BookCopy bookCopy = new BookCopy(testISBN, 2);
		Set<BookCopy> bookCopyList = new HashSet<BookCopy>();
		bookCopyList.add(bookCopy);
		List<StockBook> listBooks = null;
		try {
			storeManager.addCopies(bookCopyList);
			listBooks = storeManager.getBooks();

			for (StockBook b : listBooks) {
				if (b.getISBN() == testISBN) {
					assertTrue("Number of copies!",
							b.getNumCopies() == totalNumCopies);
					break;
				}
			}
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}

		bookCopy = new BookCopy(testISBN, 0);
		Boolean invalidNumCopiesThrewException = false;
		bookCopyList = new HashSet<BookCopy>();
		bookCopyList.add(bookCopy);
		try {
			storeManager.addCopies(bookCopyList);
		} catch (BookStoreException e) {
			invalidNumCopiesThrewException = true;
		}
		assertTrue(invalidNumCopiesThrewException);
		List<StockBook> currentList = null;
		try {
			currentList = storeManager.getBooks();
			assertTrue(currentList.equals(listBooks));
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}

		bookCopy = new BookCopy(-1, 0);
		Boolean invalidISBNThrewException = false;
		bookCopyList = new HashSet<BookCopy>();
		bookCopyList.add(bookCopy);
		try {
			storeManager.addCopies(bookCopyList);
		} catch (BookStoreException e) {
			invalidISBNThrewException = true;
		}
		assertTrue(invalidISBNThrewException);
		try {
			currentList = storeManager.getBooks();
			assertTrue(currentList.equals(listBooks));
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}

		bookCopy = new BookCopy(1000000, 0);
		Boolean nonExistingISBNThrewException = false;
		bookCopyList = new HashSet<BookCopy>();
		bookCopyList.add(bookCopy);
		try {
			storeManager.addCopies(bookCopyList);
		} catch (BookStoreException e) {
			nonExistingISBNThrewException = true;
		}
		assertTrue(nonExistingISBNThrewException);
		try {
			currentList = storeManager.getBooks();
			assertTrue(currentList.equals(listBooks));
		} catch (BookStoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Here we test updateEditorsPick and getEditorPicks functionalities.
	 * 
	 * 1. We create a book, make it an Editors pick by updateEditorsPick.
	 * 
	 * 2. Execute getEditorPicks and check that the book is returned.
	 * 
	 * 3. Now we remove this book from editor's pick.
	 * 
	 * 4. Executing getEditorPicks should now throw exception.
	 * 
	 * 5. We also test that exception is thrown if updateEditorsPick is executed
	 * with wrong arguments.
	 */
	@Test
	public void testUpdateEditorsPick() {
		Integer testISBN = 800;
		Set<StockBook> books = new HashSet<StockBook>();
		books.add(new ImmutableStockBook(testISBN, "Book Name", "Book Author",
				(float) 100, 1, 0, 0, 0, false));

		try {
			storeManager.addBooks(books);
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}

		Set<BookEditorPick> editorPicksVals = new HashSet<BookEditorPick>();
		BookEditorPick editorPick = new BookEditorPick(testISBN, true);
		editorPicksVals.add(editorPick);
		try {
			storeManager.updateEditorPicks(editorPicksVals);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		List<Book> lstEditorPicks = new ArrayList<Book>();
		try {
			lstEditorPicks = client.getEditorPicks(1);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		Boolean testISBNisInEditorPicks = false;
		for (Book book : lstEditorPicks) {
			if (book.getISBN() == testISBN)
				testISBNisInEditorPicks = true;
		}
		assertTrue("Chk if list contains testISBN!", testISBNisInEditorPicks);

		editorPicksVals.clear();
		editorPick = new BookEditorPick(testISBN, false);
		editorPicksVals.add(editorPick);
		try {
			storeManager.updateEditorPicks(editorPicksVals);
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}

		Boolean exceptionThrown = false;
		lstEditorPicks = new ArrayList<Book>();
		try {
			lstEditorPicks = client.getEditorPicks(1);
		} catch (BookStoreException e) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);

		exceptionThrown = false;
		editorPicksVals.clear();
		editorPick = new BookEditorPick(-1, false);
		editorPicksVals.add(editorPick);
		try {
			storeManager.updateEditorPicks(editorPicksVals);
		} catch (BookStoreException e) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);

		exceptionThrown = false;
		editorPicksVals.clear();
		editorPick = new BookEditorPick(1000000000, false);
		editorPicksVals.add(editorPick);
		try {
			storeManager.updateEditorPicks(editorPicksVals);
		} catch (BookStoreException e) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);

	}

	/**
	 * Here we want to test getBooksInDemand functionality
	 * 
	 * 1. We create a book with 1 copy in stock.
	 * 
	 * 2. We try to buy this book twice.
	 * 
	 * 3. First time buying should be successful but the second time buying
	 * should throw exception book not in stock.
	 * 
	 * 4. Now we execute storeManager.getBooksInDemand and check that testISBN
	 * is returned in this call.
	 */

	@Test
	public void testGetBooksInDemand() {
		Integer testISBN = 500;
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(new ImmutableStockBook(testISBN, "Book Name",
				"Book Author", (float) 100, 1, 0, 0, 0, false));
		try {
			storeManager.addBooks(booksToAdd);
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}

		Set<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(testISBN, 1));
		try {
			client.buyBooks(booksToBuy);
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}
		Boolean notInStockExceptionThrown = false;
		try {
			client.buyBooks(booksToBuy);
		} catch (BookStoreException e) {
			notInStockExceptionThrown = true;
		}
		assertTrue("Trying to buy the book second time should throw exception",
				notInStockExceptionThrown);

		List<StockBook> booksInDemand = null;
		try {
			booksInDemand = storeManager.getBooksInDemand();
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}
		Boolean listContainsTestISBN = false;
		for (StockBook b : booksInDemand) {
			if (b.getISBN() == testISBN) {
				listContainsTestISBN = true;
				break;
			}
		}
		assertTrue("testISBN should be returned by getBooksInDemand",
				listContainsTestISBN);
	}

	@AfterClass
	public static void tearDownAfterClass() {
		if (!localTest) {
			((BookStoreHTTPProxy) client).stop();
			((StockManagerHTTPProxy) storeManager).stop();
		}
	}
}
