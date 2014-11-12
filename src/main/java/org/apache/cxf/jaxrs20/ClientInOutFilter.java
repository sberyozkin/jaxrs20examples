package org.apache.cxf.jaxrs20;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.Response;


public class ClientInOutFilter implements ClientRequestFilter, ClientResponseFilter {

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		 Object entity = getEntityFromClientCache(requestContext);
		 if (entity != null) {
			 requestContext.abortWith(Response.ok(entity).build());
		 }
	}

	@Override
	public void filter(ClientRequestContext requestContext,
			           ClientResponseContext responseContext) throws IOException {
		// filter the response entity stream, etc, etc
	}
    
	private Object getEntityFromClientCache(ClientRequestContext requestContext) {
		//TODO: implement the client cache
		return null;
	}
}
