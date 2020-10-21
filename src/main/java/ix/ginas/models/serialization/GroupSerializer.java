package ix.ginas.models.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ix.core.models.Group;

import java.io.IOException;


public class GroupSerializer extends JsonSerializer<Group> {
    public GroupSerializer () {}

    public void serialize (Group group, JsonGenerator jgen,
                           SerializerProvider provider)
            throws IOException, JsonProcessingException {
        if(group!=null){
        	provider.defaultSerializeValue(group.name, jgen);
        }
    }
}
