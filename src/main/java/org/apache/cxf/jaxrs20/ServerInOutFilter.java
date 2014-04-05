package org.apache.cxf.jaxrs20;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.NameBinding;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;

/**
 * ServerInOutFilter is a server in/out filter.
 * It will be bound only to those resource methods which have the 
 * Filtered annotation (all methods in RootResource2).
 * 
 * The filter checks the security context, aborts the request if it is not set.
 * Otherwise it filters the input stream.
 * It also modifies the response before it is written back to the client
 * 
 * @author sberyozkin
 *
 */
@ServerInOutFilter.Filtered
public class ServerInOutFilter implements ContainerRequestFilter, 
    ContainerResponseFilter {

    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(value = RetentionPolicy.RUNTIME)
    @NameBinding
    public @interface Filtered { 
    }
    
    @Override
    public void filter(ContainerRequestContext ct) throws IOException {
        if (ct.getSecurityContext().getUserPrincipal() == null) {
            // abort the request
        	ct.abortWith(Response.seeOther(getSsoIdpAddress()).build());
            return;
        }
        String httpMethod = ct.getMethod();
        if (HttpMethod.POST.equals(httpMethod) 
        	|| HttpMethod.PUT.equals(httpMethod)) {
	        InputStream is = ct.getEntityStream();
	        ct.setEntityStream(new FilterInputStream(is) {
	        });
        }
    }

    @Override
    public void filter(ContainerRequestContext ctIn, ContainerResponseContext ctOut) 
    	throws IOException {
        Object entity = ctOut.getEntity();
        if (entity instanceof Book) {
            ((Book)entity).setName("name updated");
        }
    }
    
    private URI getSsoIdpAddress() {
        //TODO: implement
        return null;
    }
}
