package org.apache.cxf.jaxrs20;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

/**
 * This feature dynamically attaches ServerInOutFilter to a specific resource method only
 * @author sberyozkin
 *
 */
public class DynamicFeatureProvider implements DynamicFeature {

    @Override
    public void configure(ResourceInfo info, FeatureContext ct) {
        if (RootResource.class == info.getResourceClass()
            && "addBoook".equals(info.getResourceMethod().getName())) {
            ct.register(new ServerInOutFilter());
        }
        
    }

}
