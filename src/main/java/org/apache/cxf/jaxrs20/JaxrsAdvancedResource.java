package org.apache.cxf.jaxrs20;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.cxf.jaxrs20.model.Book;

/**
 * Advanced JAX-RS Resource which demonstrates how Request context
 * can be used to support conditional GET and StreamingOutput - streaming writes
 * 
 */
@Path("/advanced")
public class JaxrsAdvancedResource {
    // Map of ETags, key - is the Book id
	private Map<Long, EntityTag> etags = new ConcurrentHashMap<Long, EntityTag>();
    // Map of Books
	private Map<Long, Book> books = new ConcurrentHashMap<Long, Book>();
    
    
    @Context
    private Request request;
    
    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getBook(@PathParam("id") Long id) {
        // Do we have a tag
    	EntityTag tag = etags.get(id);
        if (tag == null) {
            StreamingBookJson streamBook = doGetStreamingBook(id);
            tag = calculateTag(streamBook.getBook());
            etags.put(id, tag);
            return Response.ok(streamBook).tag(tag).build();
        } else {
        	// We have a tag, evaluate if the book has been updated recently
            ResponseBuilder r = request.evaluatePreconditions(tag);
            if (r == null) {
            	// The book has been updated, return a new representation
                return Response.ok(doGetStreamingBook(id)).tag(tag).build();
            } else {
                //Let the client know its Book copy is up to date, Not-Modified
                return r.build();
            }
        }
    }
    
    private StreamingBookJson doGetStreamingBook(Long id) {
        return new StreamingBookJson(doGetBook(id));
    }
    private Book doGetBook(Long id) {
        Book book = books.get(id);
        if (book == null) {
            throw new NotFoundException();
        } else {
            return book;
        }
    }
    // Create weak ETags - they are easier to deal with
    private EntityTag calculateTag(Book book) { 
        Long tagValue = book.getId() + 37 * book.getName().hashCode();
        return EntityTag.valueOf("W/" + tagValue.toString());
    }
    
    private static class StreamingBookJson implements StreamingOutput {

        private Book book;
        public StreamingBookJson(Book book) {
            this.book = book;
        }
        
        @Override
        public void write(OutputStream os) throws IOException, WebApplicationException {
        	// The process of writing the book is supposed to be long and is 
        	// split into 4 writes. It can be done asynchronously
        	
        	// Write an opening tag
        	os.write("{".getBytes());
        	
        	// Get and write ID
            String bookIdJson = "\"id\":" + book.getId();
            os.write(bookIdJson.getBytes());
            
            // Write a separator
            os.write(",".getBytes());
            
            // Get and write Name
            String bookIdName = "\"id\":" + book.getId();
            os.write(bookIdName.getBytes());
                                    
            // Write a closing tag
            os.write("}".getBytes());
        }
        public Book getBook() {
            return book;
        }
    }
}
