package org.apache.cxf.jaxrs20;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;

import javax.ws.rs.NameBinding;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;

/**
 * ServerInOutFilter is a server in/out filter.
 * It will be bound only to those resource methods which have the 
 * InputStreamFiltered annotation.
 * 
 * The filter checks the security context, aborts the request if it is not set.
 * Otherwise it filters the input stream.
 * It also modifies the response before it is written back to the client
 * 
 * @author sberyozkin
 *
 */
@ServerInOutFilter.InputStreamFiltered
public class ServerInOutFilter implements ContainerRequestFilter, 
    ContainerResponseFilter {

    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(value = RetentionPolicy.RUNTIME)
    @NameBinding
    public @interface InputStreamFiltered { 
    }
    
    @Override
    public void filter(ContainerRequestContext in) throws IOException {
        if (in.getSecurityContext().getUserPrincipal() == null) {
            in.abortWith(Response.seeOther(getSsoIdpAddress()).build());
            return;
        }
        InputStream is = in.getEntityStream();
        in.setEntityStream(new FilterInputStream(is) {
        });
    }

    @Override
    public void filter(ContainerRequestContext in, ContainerResponseContext out) throws IOException {
        Object entity = out.getEntity();
        if (entity instanceof Book) {
            ((Book)entity).setName("name updated");
        }
    }
    
    private URI getSsoIdpAddress() {
        //TODO: implement
        return null;
    }
}
