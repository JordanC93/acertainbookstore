/**
 * 
 */
package com.acertainbookstore.server;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.BookEditorPick;
import com.acertainbookstore.business.BookRating;
import com.acertainbookstore.business.CertainBookStore;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreMessageTag;
import com.acertainbookstore.utils.BookStoreResponse;
import com.acertainbookstore.utils.BookStoreUtility;

/**
 * BookStoreHTTPMessageHandler implements the message handler class which is
 * invoked to handle messages received by the BookStoreHTTPServerUtility. It
 * decodes the HTTP message and invokes the CertainBookStore server API
 * 
 * 
 */
public class BookStoreHTTPMessageHandler extends AbstractHandler {

	@SuppressWarnings("unchecked")
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		BookStoreMessageTag messageTag;
		String numBooksString = null;
		int numBooks = -1;
		String requestURI;

		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		requestURI = request.getRequestURI();

		// Need to do request multi-plexing
		if (!BookStoreUtility.isEmpty(requestURI)
				&& requestURI.toLowerCase().startsWith("/stock")) {
			messageTag = BookStoreUtility.convertURItoMessageTag(requestURI
					.substring(6)); // the request is from store
			// manager, more
			// sophisticated security
			// features could be added
			// here
		} else {
			messageTag = BookStoreUtility.convertURItoMessageTag(requestURI);
		}
		// the RequestURI before the switch
		if (messageTag == null) {
			System.out.println("Unknown message tag");
		} else {
			switch (messageTag) {

			case ADDBOOKS:
				String xml = BookStoreUtility
						.extractPOSTDataFromRequest(request);

				Set<StockBook> bookSet = (Set<StockBook>) BookStoreUtility
						.deserializeXMLStringToObject(xml);

				BookStoreResponse bookStoreresponse = new BookStoreResponse();
				try {
					CertainBookStore.getInstance().addBooks(bookSet);
				} catch (BookStoreException ex) {
					bookStoreresponse.setException(ex);
				}
				String listBooksxmlString = BookStoreUtility
						.serializeObjectToXMLString(bookStoreresponse);
				response.getWriter().println(listBooksxmlString);
				break;

			case ADDCOPIES:
				xml = BookStoreUtility.extractPOSTDataFromRequest(request);

				Set<BookCopy> listBookCopies = (Set<BookCopy>) BookStoreUtility
						.deserializeXMLStringToObject(xml);
				bookStoreresponse = new BookStoreResponse();
				try {
					CertainBookStore.getInstance().addCopies(listBookCopies);
				} catch (BookStoreException ex) {
					bookStoreresponse.setException(ex);
				}
				listBooksxmlString = BookStoreUtility
						.serializeObjectToXMLString(bookStoreresponse);
				response.getWriter().println(listBooksxmlString);
				break;

			case LISTBOOKS:
				bookStoreresponse = new BookStoreResponse();
				bookStoreresponse.setList(CertainBookStore.getInstance()
						.getBooks());
				listBooksxmlString = BookStoreUtility
						.serializeObjectToXMLString(bookStoreresponse);
				response.getWriter().println(listBooksxmlString);
				break;

			case UPDATEEDITORPICKS:

				bookStoreresponse = new BookStoreResponse();

				try {

					String xmlStringEditorPicksValues = BookStoreUtility
							.extractPOSTDataFromRequest(request);

					Set<BookEditorPick> mapEditorPicksValues = (Set<BookEditorPick>) BookStoreUtility
							.deserializeXMLStringToObject(xmlStringEditorPicksValues);

					CertainBookStore.getInstance().updateEditorPicks(
							mapEditorPicksValues);
				} catch (BookStoreException ex) {
					bookStoreresponse.setException(ex);
				}
				listBooksxmlString = BookStoreUtility
						.serializeObjectToXMLString(bookStoreresponse);
				response.getWriter().println(listBooksxmlString);
				break;

			case BUYBOOKS:
				xml = BookStoreUtility.extractPOSTDataFromRequest(request);
				Set<BookCopy> bookCopiesToBuy = (Set<BookCopy>) BookStoreUtility
						.deserializeXMLStringToObject(new String(xml));

				// Make the purchase
				bookStoreresponse = new BookStoreResponse();
				try {
					CertainBookStore.getInstance().buyBooks(bookCopiesToBuy);
				} catch (BookStoreException ex) {
					bookStoreresponse.setException(ex);
				}
				listBooksxmlString = BookStoreUtility
						.serializeObjectToXMLString(bookStoreresponse);
				response.getWriter().println(listBooksxmlString);
				break;

			case GETBOOKS:
				xml = BookStoreUtility.extractPOSTDataFromRequest(request);
				Set<Integer> isbnSet = (Set<Integer>) BookStoreUtility
						.deserializeXMLStringToObject(xml);

				bookStoreresponse = new BookStoreResponse();
				try {
					bookStoreresponse.setList(CertainBookStore.getInstance()
							.getBooks(isbnSet));
				} catch (BookStoreException ex) {
					bookStoreresponse.setException(ex);
				}
				listBooksxmlString = BookStoreUtility
						.serializeObjectToXMLString(bookStoreresponse);
				response.getWriter().println(listBooksxmlString);
				break;

			case EDITORPICKS:
				numBooksString = URLDecoder
						.decode(request
								.getParameter(BookStoreConstants.BOOK_NUM_PARAM),
								"UTF-8");
				bookStoreresponse = new BookStoreResponse();
				try {
					numBooks = BookStoreUtility
							.convertStringToInt(numBooksString);
					bookStoreresponse.setList(CertainBookStore.getInstance()
							.getEditorPicks(numBooks));
				} catch (BookStoreException ex) {
					bookStoreresponse.setException(ex);
				}
				listBooksxmlString = BookStoreUtility
						.serializeObjectToXMLString(bookStoreresponse);
				response.getWriter().println(listBooksxmlString);
				break;

			default:
				System.out.println("Unhandled message tag");
				break;
			}
		}
		// Mark the request as handled so that the HTTP response can be sent
		baseRequest.setHandled(true);

	}
}
