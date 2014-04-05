package org.apache.cxf.jaxrs20;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;

/**
 * Simple Delegating writer. It depends on the injected Provider context to find 
 * the specific writer and delegate to it 
 * @author sberyozkin
 *
 */
public class DelegatingMessageBodyWriter<T> implements MessageBodyWriter<T> {

    @Context
    private Providers providers;
    
    @Override
    public boolean isWriteable(Class<?> cls, Type type, Annotation[] anns, MediaType mt) {
        return true;
    }

    @Override
    public void writeTo(T object, Class<?> cls, Type t, Annotation[] anns,
                        MediaType mt, MultivaluedMap<String, Object> headers, OutputStream os)
        throws IOException, WebApplicationException {
        @SuppressWarnings("unchecked")
        MessageBodyWriter<T> writer = 
            (MessageBodyWriter<T>)providers.getMessageBodyWriter(cls, t, anns, mt);
        if (writer == null) {
            throw new InternalServerErrorException();
        }
        writer.writeTo(object, cls, t, anns, mt, headers, os);
    }

    @Override
    public long getSize(T o, Class<?> cls, Type t, Annotation[] anns, MediaType mr) {
        return -1;
    }
}
