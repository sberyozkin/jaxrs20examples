package org.apache.cxf.jaxrs20;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.Future;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.client.SyncInvoker;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;

import org.apache.cxf.jaxrs20.model.Book;

/**
 * Client API examples
 * @author sberyozkin
 *
 */
public class ClientApi {
     
	public void getBook() {
        String address = "http://localhost:8080/bookstore/";
        //Client:
        Client client = ClientBuilder.newClient();
        //WebTarget:
        WebTarget target = client.target(address);
        //SyncInvoker (Invocation.Builder)
        SyncInvoker invoker = target.request("application/json");
        // Result
        Book book = invoker.get(Book.class);
        System.out.println(book.getName());
    }
    
    public void getBookResponse() {
        String address = "http://localhost:8080/bookstore/";
        Client client = ClientBuilder.newClient();
        // register a custom reader
        client.register(new BookReader());
        Response r = client.target(address)
            .path("book")
            .request("application/xml")
            .get();
        Book book = r.readEntity(Book.class);
        
        System.out.println(r.getStatus());
        System.out.println(book.getName());
    }
    
    public void postGetBookAsync() throws Exception {
        String address = "http://localhost:8080/bookstore/";
        Client client = ClientBuilder.newClient();
        try {
        	// JSON entity
        	Entity<Book> jsonEntity = Entity.json(new Book("jaxrs", 1L));
        	// AsyncInvoker
        	AsyncInvoker async = client.target(address).request("application/json").async();
            // Future
        	Future<Book> f = async.post(jsonEntity, Book.class);
            System.out.println(f.get().getName());
        } catch (ClientErrorException ex) {
            // 400-500
        } catch (ServerErrorException ex) {
            // 500+
        } catch (ResponseProcessingException ex) {
            // problems with reading the data
        } catch (ProcessingException ex) {
            // client failed to send the data/etc
        }
    }
    
    public void postGetBookAsyncHandler() throws Exception {
        String address = "http://localhost:8080/bookstore/";
        Client client = ClientBuilder.newClient();
        
        Entity<Book> jsonEntity = Entity.json(new Book("jaxrs", 1L));
        
        // Get asynchronous notifications with InvocationCallback
        client.target(address).request("application/json")
           .async()
           .post(jsonEntity, 
        		 new InvocationCallback<Book>() {

						@Override
						public void completed(Book book) {
							System.out.println(book.getName());
						}
		
						@Override
						public void failed(Throwable t) {
							if (t instanceof WebApplicationException) {
								System.out.println(((WebApplicationException)t).getResponse().getStatus());
							}
						}
			     });
            
        
    }
    
    public void run() throws Exception {
        getBook();
        getBookResponse();
        postGetBookAsync();
    }
    
    private static class BookReader implements MessageBodyReader<Book> {

        @Override
        public boolean isReadable(Class<?> c, Type t, Annotation[] anns, MediaType mt) {
            return c == Book.class;
        }

        @Override
        public Book readFrom(Class<Book> c, Type t, Annotation[] anns, MediaType mt,
                             MultivaluedMap<String, String> headers, InputStream is) throws IOException,
            WebApplicationException {
            Book book = readBook(is);
            return book;
        }
        
        private Book readBook(InputStream is) {
            //TODO: implement
            return null;
        }
    }
    
    public static void main(String[] args) throws Exception {
        new ClientApi().run();
    }
}
