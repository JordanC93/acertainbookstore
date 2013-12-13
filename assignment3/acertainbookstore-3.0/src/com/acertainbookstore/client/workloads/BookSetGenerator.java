package com.acertainbookstore.client.workloads;

import java.util.*;

import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.business.StockBook;

/**
 * Helper class to generate stockbooks and isbns modelled similar to Random
 * class
 */
public class BookSetGenerator {

    private Random random;

	public BookSetGenerator() {
        random = new Random();
	}

	/**
	 * Returns num randomly selected isbns from the input set
	 * 
	 * @param num
	 * @return
	 */
	public Set<Integer> sampleFromSetOfISBNs(Set<Integer> isbns, int num) {
        List<Integer> isbnList = new ArrayList<Integer>(isbns);
        Collections.shuffle(isbnList, random);
        Set<Integer> resultSet = new TreeSet<Integer>();
        for (int i = 0; i < num; i++) {
            resultSet.add(isbnList.get(i));
        }
        return resultSet;
	}

	/**
	 * Return num stock books. For now return an ImmutableStockBook
	 * 
	 * @param num
	 * @return
	 */
	public Set<StockBook> nextSetOfStockBooks(int num) {
		Set<StockBook> result = new TreeSet<StockBook>();
        for (int i = 0; i < num; i++) {
            int isbn = Math.abs(random.nextInt());
            String title = generateTitle();
            String author = generateAuthor();
            float price = Math.abs(random.nextFloat());
            int numBooks = random.nextInt(20);
            boolean isEditorPick = random.nextBoolean();

            result.add( new ImmutableStockBook(isbn, title, author, price, numBooks, 0, 0, 0, isEditorPick) );
        }

        return result;
	}

    private String generateAuthor() {
        String[] firstNames = new String[] {
            "J. K.", "John", "Terry", "Neil", "Simon", "Linda", "Maurice", "Alice", "Peter", "R. L."
        };
        String[] lastNames = new String[] {
            "Rowling", "Vorhaus", "Pratchett", "Gaiman", "Molineux", "Da Vinci", "DiCaprio", "Johnson", "Stine",
            "Snowden"
        };

        return String.format("%s %s", firstNames[random.nextInt(firstNames.length)],
                                      lastNames[random.nextInt(lastNames.length)]);
    }

    private String generateTitle() {
        String[] firstPart = new String[] {
            "Return of the", "Lord of the", "", "Siegfried and", "Dancing with", "So you think you can"
        };
        String[] secondPart = new String[] {
            "Jedi", "King", "Mammoth", "Rings", "Pointed Stick", "Orange", "Fight", "Dance", "Whistle",
        };
        String[] thirdPart = new String[] {
            "", "", "", "", "", ": The Golden Years", ": Revenge of the Mammoth", " With A Vengeance",
            " on Holiday", ": The Official Strategy Guide", " Club"
        };

        return String.format("%s %s%s", firstPart[random.nextInt(firstPart.length)],
                                        secondPart[random.nextInt(secondPart.length)],
                                        thirdPart[random.nextInt(thirdPart.length)]);
    }
}
