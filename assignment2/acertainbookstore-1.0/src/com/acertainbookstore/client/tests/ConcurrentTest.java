package com.acertainbookstore.client.tests;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.CertainBookStore;
import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.client.BookStoreHTTPProxy;
import com.acertainbookstore.client.StockManagerHTTPProxy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ConcurrentTest {
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

    private volatile Exception threadEx;

    @Test
    public void testConcurrentBuyAdd() {
        int testISBN = 13245;
        final int numReps = 1000;
        threadEx = null;

        Set<StockBook> stockBookSet = new HashSet<StockBook>();
        stockBookSet.add(new ImmutableStockBook(testISBN, "Book Name",
                "Author Name", (float) 100, numReps+1, 0, 0, 0, false));
        stockBookSet.add(new ImmutableStockBook(testISBN + 1, "Lord of the Rings",
                "J. K. Rowling", (float) 100, numReps+1, 0, 0, 0, false));
        stockBookSet.add(new ImmutableStockBook(testISBN + 2, "50 Shades of Blue",
                "Douglas Adams", (float) 100, numReps+1, 0, 0, 0, false));

        try {
            storeManager.addBooks(stockBookSet);
        } catch (BookStoreException e) {
            e.printStackTrace();
            fail("couldn't add prerequisite books");
        }

        Set<BookCopy> bookCopySet = new HashSet<BookCopy>();
        for (StockBook book : stockBookSet) {
            bookCopySet.add(new BookCopy(book.getISBN(), 1));
        }
        final Set<BookCopy> finalBookCopySet = bookCopySet;

        List<StockBook> origBooks = null;
        try {
            origBooks = storeManager.getBooks();
        } catch (BookStoreException e) {
            e.printStackTrace();
            fail("couldn't get books before test");
        }

        Thread buyer = new Thread(new Runnable() {
            public void run() {
                try {
                    for (int i = 0; i < numReps; i++) {
                        client.buyBooks(finalBookCopySet);
                    }
                } catch (BookStoreException e) {
                    threadEx = e;
                }
            }
        });

        Thread adder = new Thread(new Runnable() {
            public void run() {
                try {
                    for (int i = 0; i < numReps; i++) {
                        storeManager.addCopies(finalBookCopySet);
                    }
                } catch (BookStoreException e) {
                    threadEx = e;
                }
            }
        });

        try {
            buyer.start();
            adder.start();
            buyer.join();
            adder.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("threads got interrupted");
        }

        if (threadEx != null) {
            threadEx.printStackTrace();
            fail("exception thrown in thread");
        }

        List<StockBook> newBooks = null;
        try {
            newBooks = storeManager.getBooks();
        } catch (BookStoreException e) {
            e.printStackTrace();
            fail("couldn't get books after test");
        }

        assertEquals("correct number of copies in store", origBooks, newBooks);
    }
}
