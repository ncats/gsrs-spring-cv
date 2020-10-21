package ix.core.util.pojopointer;

import gov.nih.ncats.common.util.CachedSupplier;


import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

public class LambdaParseRegistry{
	static CachedSupplier<Map<String,Function<String,? extends PojoPointer>>> subURIparsers= CachedSupplier.of(()->{
		final Map<String,Function<String,? extends PojoPointer>> map = new HashMap<>();



		//Needs an argument, definitely
		map.put("map", FieldBasedLambdaArgumentParser.of("map", (p)->new MapPath(p)));

		//Can use an argument, definitely
		map.put("sort", FieldBasedLambdaArgumentParser.of("sort", (p)->new SortPath(p,false)));
		map.put("revsort", FieldBasedLambdaArgumentParser.of("revsort", (p)->new SortPath(p,true)));
		map.put("flatmap", FieldBasedLambdaArgumentParser.of("flatmap", (p)->new FlatMapPath(p)));


		map.put("distinct", FieldBasedLambdaArgumentParser.of("distinct", (p)->new DistinctPath(p)));

		//Probably doesn't need an argument
		map.put("count", FieldBasedLambdaArgumentParser.of("count", (p)->new CountPath(p)));


		//Not for collections
		map.put("$fields", FieldBasedLambdaArgumentParser.of("$fields", (p)->new FieldPath(p)));


		map.put("group", FieldBasedLambdaArgumentParser.of("group", (p)->new GroupPath(p)));

		map.put("limit", LongBasedLambdaArgumentParser.of("limit", (p)->new LimitPath(p)));
		map.put("skip", LongBasedLambdaArgumentParser.of("skip", (p)->new SkipPath(p)));

		//TODO add ApiFunction by injection later
//		try{
//			ApiFunctionFactory.getInstance(Play.application())
//			.getRegisteredFunctions()
//			.stream().forEach(rf->{
//				System.out.println("Found special Function:" + rf.getFunctionURIParser().getKey());
//				LambdaArgumentParser<?> lap = rf.getFunctionURIParser();
//				map.put(lap.getKey(), lap);
//			});
//		}catch(Exception e){
//			//there's no started application
//		}
		
		return map;
	});

	public static Function<String,? extends PojoPointer> getPojoPointerParser(final String key) throws NoSuchElementException{
		Function<String,? extends PojoPointer> parser= LambdaParseRegistry.subURIparsers.get().get(key);
		try{
			return Optional.ofNullable(parser).get();
		}catch(Exception e){
			throw new NoSuchElementException("No such function:'" + key + "'");
		}
	}


}