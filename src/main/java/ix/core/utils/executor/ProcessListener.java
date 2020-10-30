package ix.core.utils.executor;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * Listener for process progress.
 */
public interface ProcessListener {
    /**
     * The following object is about to be
     * processed.
     * @param o the object that will be re-indexed.
     */
    default void preRecordProcess(Object o){}


    /**
     * Starting a new process
     */
    default void newProcess(){};

    /**
     * Finished a process.
     */
    default void doneProcess(){};

    /**
     * The following object was
     * successfully processed.
     * @param o the object that was re-indexed.
     */
    void recordProcessed(Object o);

    /**
     * An error occurred.
     * @param t the {@link Throwable} that caused the error.
     */
    default void error(Throwable t){};

    /**
     * The total number of records that are to be processed.
     * This method will be called before {@link #doneProcess()}
     * but is not guaranteed to be be called before
     * {@link #recordProcessed(Object)} because some implementations
     * may compute the total asynchronously.
     *
     * @param total the total number of records to be processed.
     */
    default void totalRecordsToProcess(int total){};

    default void countSkipped(int numSkipped){};
    
    
    /**
     * Returns {@link ProcessListener} which fires all
     * events on this listener, as well as any listeners
     * provided.
     * @param listeners
     * @return
     */
    default ProcessListener and(ProcessListener... listeners){
        ProcessListener[] parr=
                Stream.concat(Arrays.stream(listeners),Stream.of(this))
                      .toArray(s-> new ProcessListener[s]);
        return new MultiProcessListener(parr);
    }

    public static ProcessListener doNothingListener(){
    	return (o)->{};
    }

    public static ProcessListener onCountChange(BiConsumer<Integer, Integer> recordNofM){
        
        return new ProcessListener(){
            private int tot=-1;
            private int soFar=0;
            
            @Override
            public void recordProcessed(Object o) {
                soFar++;
                recordNofM.accept(soFar, (tot>=0)?tot:null);
            }
            
            @Override
            public void totalRecordsToProcess(int tot) {
                this.tot=tot;
            }
            
        };
    }
    

    
    
}
