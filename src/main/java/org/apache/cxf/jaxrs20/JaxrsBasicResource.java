package org.apache.cxf.jaxrs20;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs20.model.Book;

/**
 * Basic JAX-RS Resource.
 * @author sberyozkin
 *
 */

@Path("/basic")
public class JaxrsBasicResource {
    private Map<Long, Book> books = new ConcurrentHashMap<Long, Book>();
    
    @Context
    private UriInfo uriInfo;
    
    // GET /basic/1, /basic/2, etc
    // Accept: application/xml
    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML})
    public Book getBook(@PathParam("id") Long id) {
        return books.get(id);
    }
    
    @GET
    @Path("search")
    @Produces({MediaType.APPLICATION_XML})
    public List<Book> findMatchingBooks(@QueryParam("name") 
                                        @DefaultValue("jaxrs") 
                                        String name) {
        List<Book> list = new LinkedList<Book>();
        for (Book b : books.values()) {
            if (b.getName().contains(name)) {
                list.add(b);
            }
        }
        return list;
    }
    
    @GET
    @Path("{id}/name")
    @Produces({MediaType.TEXT_PLAIN})
    public String getBookName(@PathParam("id") BookId id) {
        return books.get(id.getId()).getName();
    }
    
    // POST /basic
    // Content-Type: text/plain
    @POST
    @Consumes({MediaType.TEXT_PLAIN})
    public Response addBook(String name) {
        Book newBook = new Book(name, books.size() + 1);
        books.put(newBook.getId(), newBook);
        UriBuilder ub = uriInfo.getAbsolutePathBuilder();
        URI newBookURI = ub.path(Long.toString(newBook.getId())).build();
        return Response.created(newBookURI).build();
    }
    
    @Path("subresource")
    public SubResource getSub() {
        return new SubResource(); 
    }
    
    public class SubResource {
    	
    	// PUT /basic/sub
    	// Content-Type: application/x-www-form-urlencoded
        @PUT
        @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
        public void updateBook(@FormParam("id") String id, @FormParam("name") String name) {
            books.get(id).setName(name);
        }
    }
    
    public static class BookId {
        
        private long id;

        public BookId() {
            
        }
        //This constructor will be used to create BookId
        public BookId(String str) {
            id = Long.valueOf(str);
        }
        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
    }
}
