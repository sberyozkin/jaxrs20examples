package org.apache.cxf.jaxrs20;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

/**
 * This feature dynamically attaches ServerInOutFilter to a specific resource method only.
 * This approach is useful when static per-method bindings are not considered to be flexible
 * @author sberyozkin
 *
 */
@Provider
public class DynamicFeatureProvider implements DynamicFeature {

    @Override
    public void configure(ResourceInfo info, FeatureContext ct) {
        if (JaxrsBasicResource.class == info.getResourceClass()
            && "addBook".equals(info.getResourceMethod().getName())) {
            ct.register(new ServerInOutFilter());
        }
    }

}
