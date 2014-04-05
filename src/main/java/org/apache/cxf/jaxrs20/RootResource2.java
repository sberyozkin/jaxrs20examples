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

/**
 * Typical JAX-RS Resource which demonstrates the way Request context
 * can be used to support conditional GET and how StreamingOutput can be used
 * 
 * Note: it has the same Path as RootResource: JAX-RS 2.0 ensures no ambiguity
 * exists in this case.
 * 
 * @author sberyozkin
 *
 */
@Path("/root")
public class RootResource2 {
    private Map<Long, Book> books = new ConcurrentHashMap<Long, Book>();
    private Map<Long, EntityTag> etags = new ConcurrentHashMap<Long, EntityTag>();
    
    @Context
    private Request request;
    
    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getBook(@PathParam("id") Long id) {
        EntityTag tag = etags.get(id);
        if (tag == null) {
            StreamingBookJson streamBook = doGetStreamingBook(id);
            tag = calculateTag(streamBook.getBook());
            etags.put(id, tag);
            return Response.ok(streamBook).tag(tag).build();
        } else {
            ResponseBuilder r = request.evaluatePreconditions(tag);
            if (r == null) {
                return Response.ok(doGetStreamingBook(id)).tag(tag).build();
            } else {
                //Not-Modified
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
            final String bookJson = "{" 
                                    + "\"id\":" + book.getId()
                                    + ","
                                    + "\"name\":\"" + book.getName() + "\""
                                    + "}";
            os.write(bookJson.getBytes());
            
        }
        public Book getBook() {
            return book;
        }
    }
}
