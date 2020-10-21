package ix.core.search;



import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
@Slf4j
public abstract class ArgumentAdapter implements Consumer<String[]>, 
														Supplier<String[]>{
	public abstract void accept(String[] c);
	public abstract String[] get();
	public abstract String name();
	
	public static final Function<String[], String> stringLastArgParser = s->{
		if(s.length==0)return null;
 		return s[s.length-1]; //gets last element
 	};
 	public static final Function<String, String[]> stringLastArgParserInv = s->{
 		if(s==null) return new String[]{};
 		return new String[]{s};
 	};
 	
	public static final Function<String[], List<String>> stringListParser = s->{
 		return Arrays.stream(s).collect(Collectors.toList());
 	};
 	public static final Function<List<String>, String[]> stringListParserInv = l->{
 		return l.stream().toArray(i->new String[i]);
 	};
 	
 	public static final Function<String[], Optional<Double>> doubleParser = stringLastArgParser.andThen(s->{
        try{
            return Optional.of(Double.parseDouble(s));
        }catch(NumberFormatException ex){
            log.trace("Bogus double value: "+s, ex);
        }
        return Optional.empty();
    });
    public static final Function<List<String>, String[]> doubleParserInv = d->{
        return new String[]{d+""};
    };
 	
 	public static final Function<String[], Optional<Integer>> intParser = stringLastArgParser.andThen(s->{
 		try{
 			return Optional.of(Integer.parseInt(s));
 		}catch(NumberFormatException ex){
 			log.trace("Bogus integer value: "+s, ex);
 		}
 		return Optional.empty();
 	});
 	public static final Function<Integer, String[]> intParserInv = i->{
 		return new String[]{i+""};
 	};
 	
 	
 	
	public static ArgumentAdapter of(String name, Consumer<String[]> c, Supplier<String[]> sup){
		return new ArgumentAdapter(){
			@Override
			public void accept(String[] t) {
				c.accept(t);
			}
			@Override
			public String[] get() {
				return sup.get();
			}
			@Override
			public String name() {
				return name;
			}
		};
	}
	public static ArgumentAdapter ofList(String name, Consumer<List<String>> c, Supplier<List<String>> sup){
		return new ArgumentAdapter(){
			@Override
			public void accept(String[] t) {
				c.accept(stringListParser.apply(t));
			}
			@Override
			public String[] get() {
				return stringListParserInv.apply(sup.get());
			}
			@Override
			public String name() {
				return name;
			}
		};
	}
	public static ArgumentAdapter ofInteger(String name, Consumer<Integer> c, Supplier<Integer> sup){
		return new ArgumentAdapter(){
			@Override
			public void accept(String[] t) {
				intParser.apply(t).ifPresent(i->{
					c.accept(i);
				});
			}
			@Override
			public String[] get() {
				return intParserInv.apply(sup.get());
			}
			@Override
			public String name() {
				return name;
			}
		};
	}
	public static ArgumentAdapter ofSingleString(String name, Consumer<String> c, Supplier<String> sup){
		return new ArgumentAdapter(){
			@Override
			public void accept(String[] t) {
				c.accept(stringLastArgParser.apply(t));
			}
			@Override
			public String[] get() {
				return stringLastArgParserInv.apply(sup.get());
			}
			@Override
			public String name() {
				return name;
			}
		};
	}
	/**
	 * Argument adapter that parses String[] for a boolean value.
	 * Here, it assumes that only "false" will turn it to false.
	 * 
	 * @param name
	 * @param c
	 * @param sup
	 * @return
	 */
	public static ArgumentAdapter ofBoolean(String name, Consumer<Boolean> c, Supplier<Boolean> sup, boolean def){
		return new ArgumentAdapter(){
			@Override
			public void accept(String[] t) {
				String get=stringLastArgParser.apply(t);
				if("true".equalsIgnoreCase(get)){
					c.accept(true);
					return;
				}
				if("false".equalsIgnoreCase(get)){
					c.accept(false);
					return;
				}
				c.accept(def);
			}
			@Override
			public String[] get() {
				return stringLastArgParserInv.apply(sup.get()?"true":"false");
			}
			@Override
			public String name() {
				return name;
			}
		};
	}
	
	
	public static ArgumentAdapter doNothing(){
		return ofSingleString("", s->{},()->null);
	}
	
	public static String getLastStringOrElse(String[] args, String def){
	    if(args==null)return def;
	    return ArgumentAdapter.stringLastArgParser.apply(args);
	}
	public static Double getLastDoubleOrElse(String[] args, Double def){
        if(args==null)return def;
        return ArgumentAdapter.doubleParser.apply(args).orElse(def);
    }
	public static Integer getLastIntegerOrElse(String[] args, Integer def){
        if(args==null)return def;
        return ArgumentAdapter.intParser.apply(args).orElse(def);
    }
    public static Boolean getLastBooleanOrElse(String[] args, boolean def) {
        if(args==null)return def;
        return ArgumentAdapter.stringLastArgParser
                    .andThen(s->{
                        return Boolean.parseBoolean(s);   
                    })
                    .apply(args);
    }
	
}