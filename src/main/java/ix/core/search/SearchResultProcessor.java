package ix.core.search;



import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Structure searching
 */
@Slf4j
public abstract class SearchResultProcessor<T, R> extends ResultProcessor<T, R> {
	
    protected Iterator<T> results;
    
    final SearchResultContext context = new SearchResultContext (); 
    boolean wait=false;
    
    public SearchResultProcessor () {}
    
    @Deprecated
    public void setWait(boolean wait) {
		this.wait=wait;
	}


    @Deprecated
	public boolean isWait() {
		return wait;
	}


	@Override
	public SearchResultContext getContext() {
		return context;
	}


	@Override
	public void setUnadaptedResults(Iterator<T> results) {
		this.results=results;
	}
	
	@Override
	public Iterator<T> getUnadaptedResults() {
		return this.results;
	}


	@Override
	public Stream<R> map(T result) {
		try{
			R r=instrument(result);
			if(r==null)return Stream.empty();
			return Stream.of(r);
		}catch(Exception e){
			log.error("error processing record", e);
			return Stream.empty();
		}
	}
	
	protected abstract R instrument(T result) throws Exception;
	
}