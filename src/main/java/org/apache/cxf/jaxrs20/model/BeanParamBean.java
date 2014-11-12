package org.apache.cxf.jaxrs20.model;

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
    
    @FormParam("id")
    public void setId(long id) {
        this.id = id;
    }
    public long getId() {
        return id;
    }
    
    @FormParam("name")
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    
    @Context
    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }
    public UriInfo getUriInfo() {
        return uriInfo;
    }
    
}
