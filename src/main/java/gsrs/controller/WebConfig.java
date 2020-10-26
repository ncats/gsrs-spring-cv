package gsrs.controller;

import gsrs.springUtils.AutowireHelper;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.*;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcRegistrations webMvcRegistrationsHandlerMapping() {
        System.out.println("here in webmvc registration");
        return new WebMvcRegistrations() {
            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {

                return new RequestMappingHandlerMapping() {
                    private final static String API_BASE_PATH = "api/v";


                    @Override
                    protected boolean isHandler(Class<?> beanType) {
                        boolean result= AnnotatedElementUtils.hasAnnotation(beanType, GsrsRestApiController.class) /*|| super.isHandler(beanType)*/;
                        System.out.println("asking if is handler for " + beanType + "  " + result);

                        return result;
                    }

                    @Override
                    protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
                       //the handler is often a String for the package name... convert to real class
                        Class<?> handlerType = (handler instanceof String ?
                                obtainApplicationContext().getType((String) handler) : handler.getClass());

                        if (handlerType == null) {
                            return;
                        }
                            Class<?> beanType = ClassUtils.getUserClass(handlerType);

                        GsrsRestApiController gsrsRestApiAnnotation = AnnotationUtils.findAnnotation(beanType, GsrsRestApiController.class);
                        System.out.println("bean type" + beanType + "  annotation = " + gsrsRestApiAnnotation);

                        System.out.println(method);
                        GsrsRestApiGetMapping getMapping = method.getAnnotation(GsrsRestApiGetMapping.class);
                        GsrsRestApiPostMapping postMapping=null;
                        System.out.println("\t" + getMapping);
                        int[] versions=new int[]{1};
                        if(getMapping ==null){
                            //check for post
                            postMapping = method.getAnnotation(GsrsRestApiPostMapping.class);
                            if(postMapping !=null) {
                                versions = postMapping.apiVersions();
                            }
                        }else{
                            versions = getMapping.apiVersions();
                        }

                        //we want to do 2 things
                        //1. add the api base to everything
                        //2. if there's an ID use the regex for that entity type
                        Set<String> apiBasePatterns = new HashSet<>();
                        List<Set<String>> apiBasesByVersions = new ArrayList<>();
                        for(int i=0; i< versions.length; i++) {
                            Set<String> patterns = new PatternsRequestCondition(API_BASE_PATH + versions[i])
                                    .combine(mapping.getPatternsCondition()).getPatterns();
                            apiBasePatterns.addAll(patterns);
                            apiBasesByVersions.add(patterns);
                        }
                        System.out.println(apiBasesByVersions);

                        //this will be overridden if we have get or post mappings
                        PatternsRequestCondition apiPattern = new PatternsRequestCondition(apiBasePatterns.toArray(new String[apiBasePatterns.size()]));
                        if (getMapping != null) {
                           if (gsrsRestApiAnnotation != null) {


                               IdHelpers commonIdHelper = gsrsRestApiAnnotation.idHelper();
                               IdHelper idHelper;
                               if(commonIdHelper == IdHelpers.CUSTOM){
                                   String className = gsrsRestApiAnnotation.customIdHelperClassName();
                                   try {
                                       if(className ==null || className.isEmpty()){

                                           idHelper = gsrsRestApiAnnotation.customIdHelperClass().newInstance();


                                       }else{
                                           idHelper = (IdHelper) ClassUtils.forName(className, getClass().getClassLoader()).newInstance();
                                       }
                                   } catch (Exception e) {
                                       e.printStackTrace();
                                       throw new IllegalStateException("error instantiating idHelper class", e);
                                   }
                                   //inject anything if needed
                                   AutowireHelper.getInstance().autowire(idHelper);
                               }else{
                                   idHelper= commonIdHelper;
                               }

                               String idPlaceHolder = getMapping.idPlaceholder();
                               String notIdPlaceHolder = getMapping.notIdPlaceholder();

                               Set<String> updatedPatterns = new LinkedHashSet<>();
                               //Spring adds leading / to the paths if you forgot to put it
                               //but GSRS doesn't want to have it sometimes
                               // for example api/v1/context( id)  not api/v1/context/( id)
                               //so check to see if we put a leading slash and if not get rid
                               // of the leading slash which is now a middle slash

                               //now we do this for each version too!
                               Set<String> adjustedPatterns = new HashSet<>();
                               for(Set<String> basePatternPerVersion : apiBasesByVersions) {
                                   Iterator<String> patternIter = basePatternPerVersion.iterator();

                                   Iterator<String> definedInAnnotation = Arrays.asList(getMapping.value()).iterator();

                                   while (patternIter.hasNext()) {
                                       String pattern = patternIter.next();
                                       String defined = definedInAnnotation.next();

                                       if (!defined.isEmpty() && defined.charAt(0) != '/') {
                                           int offset = pattern.lastIndexOf(defined);
                                           if (offset > -1 && pattern.charAt(offset - 1) == '/') {

                                               String before = pattern.substring(0, offset - 1);
                                               adjustedPatterns.add(before + defined);
                                           }
                                       } else {
                                           adjustedPatterns.add(pattern);
                                       }
                                   }
                               }
                               for(String route : adjustedPatterns) {
                                   String updatedRoute = idHelper.replaceId(route, idPlaceHolder);
                                   updatedRoute = idHelper.replaceInverseId(updatedRoute, notIdPlaceHolder);
                                   System.out.println("updated route : " + route + "  -> " + updatedRoute);

                                   updatedPatterns.add(updatedRoute);

                               }
                               apiPattern = new PatternsRequestCondition(updatedPatterns.toArray(new String[updatedPatterns.size()]));

                            }
                            mapping = new RequestMappingInfo(mapping.getName(), apiPattern,
                                    mapping.getMethodsCondition(), mapping.getParamsCondition(),
                                    mapping.getHeadersCondition(), mapping.getConsumesCondition(),
                                    mapping.getProducesCondition(), mapping.getCustomCondition());

                        }else if(postMapping !=null){
                                if (gsrsRestApiAnnotation != null) {


                                    IdHelpers commonIdHelper = gsrsRestApiAnnotation.idHelper();
                                    IdHelper idHelper;
                                    if(commonIdHelper == IdHelpers.CUSTOM){
                                        String className = gsrsRestApiAnnotation.customIdHelperClassName();
                                        try {
                                            if(className ==null || className.isEmpty()){

                                                    idHelper = gsrsRestApiAnnotation.customIdHelperClass().newInstance();


                                            }else{
                                                idHelper = (IdHelper) ClassUtils.forName(className, getClass().getClassLoader()).newInstance();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            throw new IllegalStateException("error instantiating idHelper class", e);
                                        }
                                        //inject anything if needed
                                        AutowireHelper.getInstance().autowire(idHelper);
                                    }else{
                                        idHelper= commonIdHelper;
                                    }

                                    String idPlaceHolder = postMapping.idPlaceholder();
                                    String notIdPlaceHolder = postMapping.notIdPlaceholder();
                                    Set<String> updatedPatterns = new LinkedHashSet<>();
                                    //Spring adds leading / to the paths if you forgot to put it
                                    //but GSRS doesn't want to have it sometimes
                                    // for example api/v1/context( id)  not api/v1/context/( id)
                                    //so check to see if we put a leading slash and if not get rid
                                    // of the leading slash which is now a middle slash

                                    //now we do this for each version too!
                                    Set<String> adjustedPatterns = new HashSet<>();
                                    for(Set<String> basePatternPerVersion : apiBasesByVersions) {

                                        Iterator<String> definedInAnnotation = Arrays.asList(postMapping.value()).iterator();
                                        if(!definedInAnnotation.hasNext()){
                                            //no route this is probably a post to the root so nothing to adjust
                                            adjustedPatterns.addAll(basePatternPerVersion);
                                            continue;
                                        }
                                        Iterator<String> patternIter = basePatternPerVersion.iterator();

                                        while (patternIter.hasNext()) {
                                            String pattern = patternIter.next();
                                            System.out.println(pattern);
                                            String defined = definedInAnnotation.next();
                                            if (defined.charAt(0) != '/') {
                                                int offset = pattern.lastIndexOf(defined);
                                                if (offset > -1 && pattern.charAt(offset - 1) == '/') {

                                                    String before = pattern.substring(0, offset - 1);
                                                    adjustedPatterns.add(before + defined);
                                                }
                                            } else {
                                                adjustedPatterns.add(pattern);
                                            }
                                        }
                                    }


                                    for(String route : adjustedPatterns) {
                                        String updatedRoute = idHelper.replaceId(route, idPlaceHolder);
                                        updatedRoute = idHelper.replaceInverseId(updatedRoute, notIdPlaceHolder);
                                        System.out.println("updated route : " + route + "  -> " + updatedRoute);
                                        updatedPatterns.add(updatedRoute);

                                    }
                                    apiPattern = new PatternsRequestCondition(updatedPatterns.toArray(new String[updatedPatterns.size()]));

                                }
                                mapping = new RequestMappingInfo(mapping.getName(), apiPattern,
                                        mapping.getMethodsCondition(), mapping.getParamsCondition(),
                                        mapping.getHeadersCondition(), mapping.getConsumesCondition(),
                                        mapping.getProducesCondition(), mapping.getCustomCondition());

                            }

                        super.registerHandlerMethod(handler, method, mapping);

                    }

                    ;
                };
            }
        };

    }
}
