package ix.core.util.pojopointer;

import com.fasterxml.jackson.core.JsonPointer;

import java.io.IOException;
import java.io.Serializable;

/**
 * Path to a JSON serializable sub-element of an object. This is used
 * analogously to {{@link JsonPointer} which
 * is used to get a specific descendant JsonNode. However, unlike JsonPointer,
 * a PojoPointer may specify some additional operations, such as filters
 * for a collection, or specifying whether an object is to be returned in
 * its "raw" form, or as a JSONNode serialized form.
 * 
 * 
 * 
 * <p>
 * PojoPointer supports reading JsonPointer notation with {@link #fromJsonPointer(JsonPointer jp)}
 * </p>
 * 
 * 
 * @author peryeata
 *
 */
public interface PojoPointer extends Serializable{
    
	/**
	 * Parses the supplied JsonPointer into a PojoPointer.
	 * 
	 * @param jp
	 * @return
	 */
	public static PojoPointer fromJsonPointer(JsonPointer jp){
		final PojoPointer root=new IdentityPath();
		PojoPointer parent=root;

		for(;jp!=null;jp=jp.tail()){
			if(jp.matches()) {
				continue;
			}
			final String prop=jp.getMatchingProperty();
			final int pp=jp.getMatchingIndex();
			PojoPointer c=null;
			
			if(pp>=0){
				c = new ArrayPath(pp);
			}else if("-".equals(prop)){
			    c = new ArrayPath(-1);
            }else{
				c = new ObjectPath(prop);
			}
			parent.tail(c);
			parent=c;
		}
		return root;
	}

	/**
	 * Parses the supplied string to a JsonPointer, and then
	 * into a PojoPointer.
	 * 
	 * @param jp
	 * @return
	 */
	public static PojoPointer fromJsonPointer(final String jp){
		return PojoPointer.fromJsonPointer(JsonPointer.compile(jp));
	}


	
	/**
	 * ABNF:
	 * <pre>
	 *  URIPOJOPOINTER   = *( OBJECTLOCATOR *SPECIALLOCATOR)
	 *  SPECIALLOCATOR   = ARRAYLOCATOR / FILTERLOCATOR / FUNCTIONLOCATOR / IDLOCATOR
	 *  FILTERLOCATOR    = "(" URIPOJOPOINTER ":" FUNCTIONARGUMENT ")"
	 *  IDLOCATOR        = "(" ID ")"
	 *  FUNCTIONLOCATOR  = "!" FUNCTIONNAME "(" FUNCTIONARGUMENT ")"
	 *  FUNCTIONNAME     = *FIELDCHAR
	 *  FUNCTIONARGUMENT = "(" FUNCTIONARGUMENT ")" / *ALLOWEDCHARS
	 *  ALLOWEDCHARS     = DIGIT / ALPHA / "$" / "/" / "!" / ":" / "-" / "=" / "^" / "*" / "~" / "_" / "," / " "
	 *  OBJECTLOCATOR    = *("/" *1RAWSIGNIFIER FIELDSIGNIFIER) 
	 *  FIELDSIGNIFIER   = [STARTFIELD *FIELDCHAR]
	 *  STARTFIELD       = ALPHA
	 *  FIELDCHAR        = ALPHA / DIGIT / "$" / "_"
	 *  RAWSIGNIFIER     = "$"
	 *  ARRAYLOCATOR     = "($" 1*DIGIT ")"
	 *  ID               = 1*(ALPHA / DIGIT / "-")
	 *  ALPHA            = %x41-5A / %x61-7A   ; A-Z / a-z
	 *  DIGIT            = %x30-39             ; 0-9
	 * </pre> 
	 * 
	 *  
	 * @param uripath
	 * @return
	 */
	public static PojoPointer fromURIPath(String uripath){
		return URIPojoPointerParser.fromURI(uripath);
	}

	
	default boolean hasTail(){
		final PojoPointer tail = tail();
		return tail!=null;
	}

	default boolean isLeafRaw(){
		PojoPointer pp=this;
		while(pp.hasTail()){
			pp=pp.tail();
		}
		return pp.isRaw();
	}

	public boolean isRaw();
	public void setRaw(boolean raw);
	public void tail(PojoPointer pp);
	public PojoPointer tail();
	
	default PojoPointer headOnly(){
	    PojoPointer pclone = fromURIPath(toURIpath());
	    while(pclone instanceof IdentityPath){
	        pclone = pclone.tail();
	    }
	    pclone.tail(null);
	    return pclone;
	}
	
	public JsonPointer toJsonPointer();
	
	public String toURIpath();
	
	default void writeObject(java.io.ObjectOutputStream out) throws IOException{
	    out.writeUTF(toURIpath());
	}
	
	
	
}