package edu.ucsb.cs.nsm.model;

/**
 * 
 * @author abaki
 *
 */
@Deprecated
public class Cookie {
	private String name;
	private String value;
	private String domain;
	private String path;
	private String expires;
	
	public Cookie() {
		
	}
	public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }
	
	public Cookie(String name, String value, String domain, String path, String expires) {
        this(name, value);
        this.domain = domain;
        this.path = path;
        this.expires = expires;
    }
	
	public String getName() {
        return name;
    }
	
	public void setName(String name) {
		this.name = name;
	}

    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
    	this.value = value;
    }

    public String getDomain ( ) {
        return domain;
    }
    
    public void setDomain(String domain) {
    	this.domain = domain;
    }

    public String getPath ( ) {
        return path;
    }
    
    public void setPath(String path) {
    	this.path = path;
    }

    public String getExpires ( ) {
        return expires;
    }
    
    public void setExpires(String expires) {
    	this.expires = expires;
    }
    
    public boolean equals(Object other) {
        if (other instanceof Cookie) {
            Cookie otherCookie = (Cookie) other;
            if (otherCookie.name.equals(this.name)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        return name.hashCode();
    }
    
    public String toString() {
    	return this.name+"="+this.value + "; expires=" + this.expires + 
    			"; path=" + this.path + "; domain=" + this.domain;
    }
}
