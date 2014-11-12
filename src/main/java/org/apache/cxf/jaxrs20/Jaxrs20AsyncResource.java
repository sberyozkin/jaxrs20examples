package org.apache.cxf.jaxrs20;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.CompletionCallback;
import javax.ws.rs.container.ConnectionCallback;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.container.TimeoutHandler;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.cxf.jaxrs20.model.BeanParamBean;
import org.apache.cxf.jaxrs20.model.Book;

/**
 * Root resource which supports asynchronous/suspended invocations
 * @author sberyozkin
 *
 */
@Path("/async")
@ServerInOutFilter.Filtered
public class Jaxrs20AsyncResource {
    // Pending AsyncResponses 
	private Map<Long, AsyncResponse> asyncs = new ConcurrentHashMap<Long, AsyncResponse>();
    // List of Books
	private Map<Long, Book> books = new ConcurrentHashMap<Long, Book>();
	@Context
	private SecurityContext securityContext;
    
    /**
     * The invocation will be resumed immediately if Book is available.
     * If not then the invocation is suspended indefinitely and resumed 
     * once the Book is available (see addBookResume)
     */
    @GET
    @Path("/book/{id}/resume")
    @Produces("application/xml")
    public void getBookResume(@Suspended AsyncResponse async, 
                              @PathParam("id") long id) {
        Book book = books.get(id);
        if (book != null) {
            async.resume(book);
        } else {
        	String userName = securityContext.getUserPrincipal().getName();
        	async.register(new ResponseCallbackImpl(userName, id));
            asyncs.put(id, async);
        }
    }
    
    @POST
    @Path("/book/{id}/resume")
    @Consumes("application/xml")
    public Response addBookResume(Book book) {
        books.put(book.getId(), book);
        // Check if there is a pending AsyncResponse expecting this Book
        AsyncResponse async = asyncs.remove(book.getId());
        if (async != null) {
        	// let it pick the expected book
            async.resume(book);
        }
        return Response.status(201).build();
    }
    
    @POST
    @Path("/book/{id}/resume")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addBookFromFormResume(BeanParamBean formBean) {
        return addBookResume(new Book(formBean.getName(), formBean.getId()));
    }
    
    /**
     * Invocation is suspended for 2 seconds. TimeoutHandler will be invoked
     * when it is resumed, the handler will either postpone it again, 
     * or return the Book, or cancel the invocation (503 status, Retry-After)
     */
    @GET
    @Path("/book/{id}/timeout")
    public void getBookWithTimeout(@Suspended AsyncResponse async,
                                   @PathParam("id") long id) {
    	// Set 2 sec timeout and register TimeoutHandler
        TimeoutHandlerImpl handler = new TimeoutHandlerImpl(id);
        async.register(handler);
        async.setTimeout(2000, TimeUnit.MILLISECONDS);
        // start asynchronous job to get the book 
        retrieveBookFromRemoteStorage(handler);
    }
    
    private void retrieveBookFromRemoteStorage(final TimeoutHandlerImpl handler) {
        // start a worker thread to retrieve a Book
        // update the books map when ready, or if not found - 
        // set a 'notAvailable' status on the handler
    }
    
    public class TimeoutHandlerImpl implements TimeoutHandler {
        private static final int MAX_WAIT_TIME_SEC = 10;
        
        private long id;
        private AtomicInteger alreadyWaitedFor = new AtomicInteger(); 
        private boolean notAvailable;
        
        public TimeoutHandlerImpl(long id) {
            this.id = id;
        }
        
        @Override
        // This method is invoked after the last timeout has expired
        public void handleTimeout(AsyncResponse asyncResponse) {
        	
            Book book = books.get(id);
            if (book == null) {
            	// if Book is known to be not available - return 404
                if (notAvailable) {
                    asyncResponse.resume(new NotFoundException());
                    return;
                }
                if (alreadyWaitedFor.addAndGet(2) <= MAX_WAIT_TIME_SEC) {
                	// still within the limits - do another timeout
                    asyncResponse.setTimeout(2000, TimeUnit.SECONDS);
                } else {
                	// cancel the request and ask the client to retry in 10 secs
                    asyncResponse.cancel(10);
                }
            } else {
                asyncResponse.resume(book);
            } 
            
        }

        public void setNotAvailable(boolean notAvailable) {
            this.notAvailable = notAvailable;
        }
        
    }
    
    private class ResponseCallbackImpl implements CompletionCallback, ConnectionCallback {
    	private long bookId;
    	private String userName;
        public ResponseCallbackImpl(String userName, long bookId) {
        	this.bookId = bookId;
        }
        @Override
        public void onComplete(Throwable t) {
        	if (t != null) {
                System.out.println(String.format("The server failed to send a book %d back to the client %s, cause is: %s", 
        	                       bookId, userName, t.getCause().getMessage()));
        	}
        }
		@Override
		public void onDisconnect(AsyncResponse disconnected) {
			System.out.println(String.format("The book %d can not be delivered, client %s disconnected", bookId, userName));
		}
        
    }
}
