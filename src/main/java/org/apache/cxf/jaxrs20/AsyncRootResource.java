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
import javax.ws.rs.container.Suspended;
import javax.ws.rs.container.TimeoutHandler;
import javax.ws.rs.core.Response;

/**
 * Root resource which supports asynchronous/suspended invocations
 * @author sberyozkin
 *
 */
@Path("/root")
public class AsyncRootResource {
    private Map<Long, AsyncResponse> asyncs = new ConcurrentHashMap<Long, AsyncResponse>();
    private Map<Long, Book> books = new ConcurrentHashMap<Long, Book>();
    
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
            asyncs.put(id, async);
        }
    }
    @POST
    @Path("/book/{id}/resume")
    @Consumes("application/xml")
    public Response addBookResume(Book book) {
        books.put(book.getId(), book);
        AsyncResponse async = asyncs.remove(book.getId());
        if (async != null) {
            async.resume(book);
        }
        return Response.status(201).build();
    }
    
    /**
     * Invocation is suspended for 2 seconds. TimeoutHandler will be invoked
     * when it is resumed, the handler will either postpone it again, or return the Book,
     * or cancel the invocation (503 status, Retry-After)
     */
    @GET
    @Path("/book/{id}/timeout")
    public void getBookWithTimeout(@Suspended AsyncResponse async,
                                   @PathParam("id") long id) {
        TimeoutHandlerImpl handler = new TimeoutHandlerImpl(id);
        async.register(handler);
        async.setTimeout(2000, TimeUnit.MILLISECONDS);
        retrieveBookFromRemoteStorage(handler);
    }
    
    private void retrieveBookFromRemoteStorage(TimeoutHandlerImpl handler) {
        // start a worker thread to retrieve a Book
        // update the books map when ready, or if not found - 
        // set the status on the handler
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
        public void handleTimeout(AsyncResponse asyncResponse) {
            if (notAvailable) {
                asyncResponse.resume(new NotFoundException());
                return;
            }
            
            Book book = books.get(id);
            if (book == null) {
                if (alreadyWaitedFor.addAndGet(2) <= MAX_WAIT_TIME_SEC) {
                    asyncResponse.setTimeout(2000, TimeUnit.SECONDS);
                } else {
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
}
