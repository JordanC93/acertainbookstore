/**
 * 
 */
package com.acertainbookstore.client.workloads;

import java.util.*;
import java.util.concurrent.Callable;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.CertainBookStore;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreException;

/**
 * 
 * Worker represents the workload runner which runs the workloads with
 * parameters using WorkloadConfiguration and then reports the results
 * 
 */
public class Worker implements Callable<WorkerRunResult> {
	private WorkloadConfiguration configuration = null;
	private int numSuccessfulFrequentBookStoreInteraction = 0;
	private int numTotalFrequentBookStoreInteraction = 0;

	public Worker(WorkloadConfiguration config) {
		configuration = config;
	}

	/**
	 * Run the appropriate interaction while trying to maintain the configured
	 * distributions
	 * 
	 * Updates the counts of total runs and successful runs for customer
	 * interaction
	 * 
	 * @param chooseInteraction
	 * @return
	 */
	private boolean runInteraction(float chooseInteraction) {
		try {
			if (chooseInteraction < configuration
					.getPercentRareStockManagerInteraction()) {
				runRareStockManagerInteraction();
			} else if (chooseInteraction < configuration
					.getPercentFrequentStockManagerInteraction()) {
				runFrequentStockManagerInteraction();
			} else {
				numTotalFrequentBookStoreInteraction++;
				runFrequentBookStoreInteraction();
				numSuccessfulFrequentBookStoreInteraction++;
			}
		} catch (BookStoreException ex) {
			// ex.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Run the workloads trying to respect the distributions of the interactions
	 * and return result in the end
	 */
	public WorkerRunResult call() throws Exception {
		int count = 1;
		long startTimeInNanoSecs = 0;
		long endTimeInNanoSecs = 0;
		int successfulInteractions = 0;
		long timeForRunsInNanoSecs = 0;

		Random rand = new Random();
		float chooseInteraction;

		// Perform the warmup runs
		while (count++ <= configuration.getWarmUpRuns()) {
			chooseInteraction = rand.nextFloat() * 100f;
			runInteraction(chooseInteraction);
		}

		count = 1;
		numTotalFrequentBookStoreInteraction = 0;
		numSuccessfulFrequentBookStoreInteraction = 0;

		// Perform the actual runs
		startTimeInNanoSecs = System.nanoTime();
		while (count++ <= configuration.getNumActualRuns()) {
			chooseInteraction = rand.nextFloat() * 100f;
			if (runInteraction(chooseInteraction)) {
				successfulInteractions++;
			}
		}
		endTimeInNanoSecs = System.nanoTime();
		timeForRunsInNanoSecs += (endTimeInNanoSecs - startTimeInNanoSecs);
		return new WorkerRunResult(successfulInteractions,
				timeForRunsInNanoSecs, configuration.getNumActualRuns(),
				numSuccessfulFrequentBookStoreInteraction,
				numTotalFrequentBookStoreInteraction);
	}

	/**
	 * Runs the new stock acquisition interaction
	 * 
	 * @throws BookStoreException
	 */
	private void runRareStockManagerInteraction() throws BookStoreException {
        StockManager stockManager = configuration.getStockManager();
        List<StockBook> books = stockManager.getBooks();
        Set<Integer> bookISBNs = new HashSet<Integer>();
        for (int i = 0; i < books.size(); i++) {
            bookISBNs.add(books.get(i).getISBN());
        }

        BookSetGenerator bookSetGenerator = configuration.getBookSetGenerator();
        Set<StockBook> generatedBooks = bookSetGenerator.nextSetOfStockBooks(configuration.getNumBooksToAdd());
        Set<StockBook> booksToAdd = new HashSet<StockBook>();

        for (StockBook book : generatedBooks) {
            if (!bookISBNs.contains(book.getISBN())) {
                booksToAdd.add(book);
            }
        }

        stockManager.addBooks(booksToAdd);
    }

	/**
	 * Runs the stock replenishment interaction
	 * 
	 * @throws BookStoreException
	 */
	private void runFrequentStockManagerInteraction() throws BookStoreException {
        StockManager stockmanager = configuration.getStockManager();
        List<StockBook> books = stockmanager.getBooks();
        Set<BookCopy> booksToAdd = new HashSet<BookCopy>();
        int k = configuration.getLowStockThreshold();

        // Create a min priority queue in order to get the k books with lowest stock
        PriorityQueue<StockBook> lowestStockBooks = new PriorityQueue<StockBook>(k+1,
                new Comparator<StockBook>() {
                    @Override
                    public int compare(StockBook stockBook, StockBook stockBook2) {
                        return Integer.compare(stockBook.getNumCopies(), stockBook2.getNumCopies());
                    }
                });

        for (StockBook book : books) {
            if (lowestStockBooks.size() > k) {
                lowestStockBooks.poll();
            }

            lowestStockBooks.add(book);
        }

        for (StockBook book : lowestStockBooks) {
            booksToAdd.add(new BookCopy(book.getISBN(), configuration.getNumBooksToAdd()));
        }

        stockmanager.addCopies(booksToAdd);
	}

	/**
	 * Runs the customer interaction
	 * 
	 * @throws BookStoreException
	 */
	private void runFrequentBookStoreInteraction() throws BookStoreException {
        BookStore store = configuration.getBookStore();
        List<Book> editorPicks = store.getEditorPicks(configuration.getNumEditorPicksToGet());
        Collections.shuffle(editorPicks);

        Set<BookCopy> booksToBuy = new HashSet<BookCopy>();
        for (int i = 0; i < configuration.getNumBooksToBuy(); i++) {
            Book book = editorPicks.get(i);
            booksToBuy.add( new BookCopy( book.getISBN(), configuration.getNumBooksToBuy() ) );
        }
        store.buyBooks(booksToBuy);
    }

}
