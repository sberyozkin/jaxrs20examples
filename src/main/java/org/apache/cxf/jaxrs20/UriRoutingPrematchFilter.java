package org.apache.cxf.jaxrs20;

import java.io.IOException;
import java.net.URI;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;

/**
 * This server filter is run before the selection is done and
 * it updates the request URI.
 * @author sberyozkin
 *
 */
@PreMatching
@Priority(Priorities.USER)
public class UriRoutingPrematchFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
    	URI uri = context.getUriInfo().getRequestUri();
    	if (uri.getPath().equals("/v0/basic")) {
            context.setRequestUri(URI.create("/v1/basic"));
    	}
    }
}
