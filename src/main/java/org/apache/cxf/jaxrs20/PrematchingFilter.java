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
 * For example, one can do it to get the request dispatched to the right method.
 * @author sberyozkin
 *
 */
@PreMatching
@Priority(Priorities.USER)
public class PrematchingFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        context.setRequestUri(URI.create("/base/uri"));
    }
}
