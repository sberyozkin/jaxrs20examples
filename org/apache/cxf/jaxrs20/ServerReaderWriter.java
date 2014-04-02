package org.apache.cxf.jaxrs20;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

/**
 * Reader/writer inbterceptor: typically used to pre-process read or write operations
 * @author sberyozkin
 *
 */
public class ServerReaderWriter implements WriterInterceptor, ReaderInterceptor {

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext ct) throws IOException, WebApplicationException {
        if (MediaType.APPLICATION_ATOM_XML_TYPE.equals(ct.getMediaType())) {
        	// invoke the next reader interceptor
            return ct.proceed();
        } else {
        	// block reading the stream
        	return null;
        }
    }

    @Override
    public void aroundWriteTo(WriterInterceptorContext ct) throws IOException, WebApplicationException {
        ct.proceed();
    }
}
