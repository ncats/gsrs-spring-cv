# GSRS 3.0 

This is a GSRS implementation using Spring Boot 2

## Attempt at maintaining Backwards Compatibility

An attempt has been made to make sure that the REST API is close enough to
the GSRS 2.x codebase so that any REST clients don't have to change.  

### REST API Clients shouldn't have to change
API Routes for fetching entities by id, or searches should use the same URIs and return either identical JSON responses
or at least similar enough that the same JSON parsing code in the client does not have to change. 

### GSRS backend should be easy to port
While the backend GSRS is substantially different between version 2 and 3, customized
GSRS code should have an easy migration path thanks to Spring's Dependency Injection. 

## How to Run
This is a Spring Boot application and can be run using the provided maven wrapper like so:
```
./mvnw spring-boot:run
```

Or from inside your IDE, you can run the main method on the Application class in the gsrs package.
## Configuration File

To maintain backwards compatibility with previous version of GSRS,
The configuration file is in HOCON format and by default 
will look for `application.conf`.

Default configuration is in the `gsrs-core.conf` file so your `application.conf`
should start with:
```
include "gsrs-core.conf"

#put your customization and overrides here:
```

## Customizations:

### Custom IndexValueMakers
  The `ix.core.search.text.IndexValueMaker<T>` interface is a way to generate custom lucene indexed fields
  to the Document representing an entity of Type T.
  
  To add an implementation just annotate your class as a `@Component` so it gets picked up by the Spring component scan:
  
  ```java
@Component
public class MyIndexValueMaker implements IndexValueMaker<Foo>{
     ...
} 
```

### EntityProcessor
GSRS uses the `ix.core.EntityProcessor<T>` interface to provide hooks for
JPA pre and post hooks when an Entity's data is changed in the database.

To add an implementation just annotate your class as a `@Component` so it gets picked up by the Spring component scan:
  
  ```java
@Component
public class MyEntityProcessor implements EntityProcessor<Foo>{
     ...
} 
```