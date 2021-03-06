/**
 * 
 */
package com.acertainbookstore.client.workloads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.acertainbookstore.business.CertainBookStore;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.client.BookStoreHTTPProxy;
import com.acertainbookstore.client.StockManagerHTTPProxy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;

/**
 * 
 * CertainWorkload class runs the workloads by different workers concurrently.
 * It configures the environment for the workers using WorkloadConfiguration
 * objects and reports the metrics
 * 
 */
public class CertainWorkload {

	/**
	 * @param args
	 */
    private static int threads = 0;
	public static void main(String[] args) throws Exception {

        for (threads = 1; threads <= 15; threads += 1) {
        int numConcurrentWorkloadThreads = threads;
		String serverAddress = "http://localhost:8081";
		boolean localTest = true;
		List<WorkerRunResult> workerRunResults = new ArrayList<WorkerRunResult>();
		List<Future<WorkerRunResult>> runResults = new ArrayList<Future<WorkerRunResult>>();

		initializeBookStoreData(serverAddress, localTest);

		ExecutorService exec = Executors
				.newFixedThreadPool(numConcurrentWorkloadThreads);

		for (int i = 0; i < numConcurrentWorkloadThreads; i++) {
			// The server address is ignored if localTest is true
			WorkloadConfiguration config = new WorkloadConfiguration(
					serverAddress, localTest);
			Worker workerTask = new Worker(config);
			// Keep the futures to wait for the result from the thread
			runResults.add(exec.submit(workerTask));
		}

		// Get the results from the threads using the futures returned
		for (Future<WorkerRunResult> futureRunResult : runResults) {
			WorkerRunResult runResult = futureRunResult.get(); // blocking call
			workerRunResults.add(runResult);
		}

		exec.shutdownNow(); // shutdown the executor
		reportMetric(workerRunResults);
        }
	}

	/**
	 * Computes the metrics and prints them
	 * 
	 * @param workerRunResults
	 */
	public static void reportMetric(List<WorkerRunResult> workerRunResults) {

        double latency = 0;
        double throughput = 0;
        int totalInteractions = 0;
        int successfulInteractions = 0;
        double ratioInteractions;
        int totalCustomerInteractions = 0;
        int successCustomerInteractions = 0;
        double ratioCustomer;
        double ratioCustomerSuccess;

        for (WorkerRunResult workerRunResult : workerRunResults) {
            long time = workerRunResult.getElapsedTimeInNanoSecs();

            throughput += (double) workerRunResult.getSuccessfulInteractions() / time;
            latency += time;
            totalInteractions += workerRunResult.getTotalRuns();
            successfulInteractions += workerRunResult.getSuccessfulInteractions();
            totalCustomerInteractions += workerRunResult.getTotalFrequentBookStoreInteractionRuns();
            successCustomerInteractions += workerRunResult.getSuccessfulFrequentBookStoreInteractionRuns();
        }


        // Calculate ratios
        latency /= totalInteractions;
        ratioInteractions = (double) successfulInteractions / totalInteractions;
        ratioCustomer = (double) totalCustomerInteractions / totalInteractions;
        ratioCustomerSuccess = (double) successCustomerInteractions / totalCustomerInteractions;

        double nanoToMilli = 1000000.0;
        System.out.println("Workload stats");
        System.out.println("------------------------------------");
        System.out.println(String.format("Number of threads:\t%d", threads));
        System.out.println(String.format("Aggregated throughput:\t%f", throughput * nanoToMilli));
        System.out.println(String.format("Average latency:\t%f", latency / nanoToMilli));
        System.out.println(String.format("Ratio of successful interactions:\t%f", ratioInteractions));
        System.out.println(String.format("Ratio of customer interactions:\t%f", ratioCustomer));
        System.out.println(String.format("Ratio of successful customer interactions:\t%f", ratioCustomerSuccess));
        System.out.println("------------------------------------");
        System.out.println("");
	}

	/**
	 * Generate the data in bookstore before the workload interactions are run
	 * 
	 * Ignores the serverAddress if its a localTest
	 * 
	 * @param serverAddress
	 * @param localTest
	 * @throws Exception
	 */
	public static void initializeBookStoreData(String serverAddress,
			boolean localTest) throws Exception {
		BookStore bookStore = null;
		StockManager stockManager = null;

		// Initialize the RPC interfaces if its not a localTest
		if (localTest) {
            // Clear book store instance before each test (a bit hacky, but eh.)
            CertainBookStore.refreshInstance();

			stockManager = CertainBookStore.getInstance();
			bookStore = CertainBookStore.getInstance();
		} else {
            stockManager = new StockManagerHTTPProxy(serverAddress + "/stock");
			bookStore = new BookStoreHTTPProxy(serverAddress);

            // Even hackier way of clearing the instance. #yolo
            ((StockManagerHTTPProxy)stockManager).resetInstance();
		}

        BookSetGenerator bookSetGenerator = new BookSetGenerator();
        stockManager.addBooks(bookSetGenerator.nextSetOfStockBooks(100));

		// Finished initialization, stop the clients if not localTest
		if (!localTest) {
			((BookStoreHTTPProxy) bookStore).stop();
			((StockManagerHTTPProxy) stockManager).stop();
		}

	}
}
