package org.apache.cxf.jaxrs20.model;

/**
 * Basic application bean
 * @author sberyozkin
 *
 */
public class Book {
    private String name;
    private long id;
    public Book() {
        
    }
    public Book(String name, long id) {
        this.name = name;
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
}
