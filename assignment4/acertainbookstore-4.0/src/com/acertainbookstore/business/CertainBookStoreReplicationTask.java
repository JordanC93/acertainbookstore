package com.acertainbookstore.business;

import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreMessageTag;
import com.acertainbookstore.utils.BookStoreUtility;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;

import java.util.concurrent.Callable;

/**
 * CertainBookStoreReplicationTask performs replication to a slave server. It
 * returns the result of the replication on completion using ReplicationResult
 */
public class CertainBookStoreReplicationTask implements
        Callable<ReplicationResult> {

    private String slaveServerAddress;
    private ReplicationRequest request;

    public CertainBookStoreReplicationTask(String slaveServerAddress, ReplicationRequest request) {
        this.slaveServerAddress = slaveServerAddress;
        this.request = request;
    }

    @Override
    public ReplicationResult call() throws Exception {
        String serializedRequest = BookStoreUtility.serializeObjectToXMLString(request);
        Buffer requestContent = new ByteArrayBuffer(serializedRequest);
        ContentExchange exchange = new ContentExchange();
        HttpClient httpClient = new HttpClient();

        String urlString = slaveServerAddress + "/" + BookStoreMessageTag.REPLICATE;
        exchange.setMethod("POST");
        exchange.setURL(urlString);
        exchange.setRequestContent(requestContent);
        try {
            BookStoreUtility.SendAndRecv(httpClient, exchange);
            return new ReplicationResult(slaveServerAddress, true);
        } catch (BookStoreException e) {
            return new ReplicationResult(slaveServerAddress, false);
        }
    }
}