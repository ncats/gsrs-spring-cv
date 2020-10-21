package ix.core.models;

import java.lang.annotation.*;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
public @interface Indexable {
    boolean indexed() default true;
    boolean sortable() default false;
    boolean taxonomy() default false;
    boolean facet() default false;
    boolean suggest() default false;
    boolean sequence() default false;
	boolean structure() default false;
	boolean fullText() default true;
    String pathsep() default "/"; // path separator for
    // if empty, use the instance variable name
    String name() default "";
    long[] ranges() default {};
    double[] dranges() default {};
    String format() default "%1$.2f"; // how to format the value?

    boolean recurse()  default true; //allow recursion below this
	boolean indexEmpty() default false; //allow indexing of ""
	
	String emptyString() default "<EMTPY>"; //String to use on empty

    /**
     * Use the full path to this field
     * as the value to be indexed.
     * This is useful when several of the same Indexable are present
     * in the object graph and you want to differentiate them
     * in the index.
     *
     * defaults to {@code false}.
     * @return
     */
    boolean useFullPath() default false;
}
