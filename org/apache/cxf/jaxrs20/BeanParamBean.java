package org.apache.cxf.jaxrs20;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 * Basic bean capturing various request properties
 * @author sberyozkin
 *
 */
public class BeanParamBean {
    private UriInfo uriInfo;
    
    private long id;
    private String name;
    public long getId() {
        return id;
    }
    @FormParam("id")
    public void setId(long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    @FormParam("name")
    public void setName(String name) {
        this.name = name;
    }
    public UriInfo getUriInfo() {
        return uriInfo;
    }
    @Context
    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }
    
    
}
