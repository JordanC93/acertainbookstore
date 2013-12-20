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

import com.acertainbookstore.business.ReplicationRequest;
import com.acertainbookstore.utils.*;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.acertainbookstore.business.SlaveCertainBookStore;

/**
 * 
 * SlaveBookStoreHTTPMessageHandler implements the message handler class which
 * is invoked to handle messages received by the slave book store HTTP server It
 * decodes the HTTP message and invokes the SlaveCertainBookStore API
 * 
 */
public class SlaveBookStoreHTTPMessageHandler extends AbstractHandler {

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

			// Write requests should not be handled
			switch (messageTag) {

			case LISTBOOKS:
				BookStoreResponse bookStoreresponse = new BookStoreResponse();
				try {
					bookStoreresponse.setResult(SlaveCertainBookStore
							.getInstance().getBooks());
				} catch (BookStoreException ex) {
					bookStoreresponse.setException(ex);
				}
				response.getWriter().println(
						BookStoreUtility
								.serializeObjectToXMLString(bookStoreresponse));
				break;

			case GETBOOKS:
				String xml = BookStoreUtility
						.extractPOSTDataFromRequest(request);
				Set<Integer> isbnSet = (Set<Integer>) BookStoreUtility
						.deserializeXMLStringToObject(xml);

				bookStoreresponse = new BookStoreResponse();
				try {
					bookStoreresponse.setResult(SlaveCertainBookStore
							.getInstance().getBooks(isbnSet));
				} catch (BookStoreException ex) {
					bookStoreresponse.setException(ex);
				}
				response.getWriter().println(
						BookStoreUtility
								.serializeObjectToXMLString(bookStoreresponse));
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
					bookStoreresponse.setResult(SlaveCertainBookStore
							.getInstance().getEditorPicks(numBooks));
				} catch (BookStoreException ex) {
					bookStoreresponse.setException(ex);
				}
				response.getWriter().println(
						BookStoreUtility
								.serializeObjectToXMLString(bookStoreresponse));
				break;

            case REPLICATE:
                String xmlReplicationRequest = BookStoreUtility.extractPOSTDataFromRequest(request);
                ReplicationRequest replicationRequest =
                        (ReplicationRequest) BookStoreUtility.deserializeXMLStringToObject(xmlReplicationRequest);

                bookStoreresponse = new BookStoreResponse();
                try {
                    BookStoreResult res = SlaveCertainBookStore.getInstance().applyReplicationRequest(replicationRequest);
                    bookStoreresponse.setResult(res);
                } catch (BookStoreException ex) {
                    bookStoreresponse.setException(ex);
                }

                response.getWriter().println(
                        BookStoreUtility
                                .serializeObjectToXMLString(bookStoreresponse));

			default:
				System.out.println("Unhandled message tag");
				break;
			}
		}
		// Mark the request as handled so that the HTTP response can be sent
		baseRequest.setHandled(true);

	}
}
