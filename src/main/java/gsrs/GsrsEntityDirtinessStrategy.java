package gsrs;

import org.hibernate.CustomEntityDirtinessStrategy;
import org.hibernate.Session;
import org.hibernate.persister.entity.EntityPersister;

import java.util.Set;

public class GsrsEntityDirtinessStrategy implements CustomEntityDirtinessStrategy {
    @Override
    public boolean canDirtyCheck(Object o, EntityPersister entityPersister, Session session) {
        return canCast(o) && !((GsrsManualDirtyMaker)o).getDirtyFields().isEmpty();
    }

    private boolean canCast(Object o) {
        return (o instanceof GsrsManualDirtyMaker);
    }


    @Override
    public boolean isDirty(Object o, EntityPersister entityPersister, Session session) {
        if(canCast(o)){
            if(!((GsrsManualDirtyMaker)o).getDirtyFields().isEmpty()){
                return true;
            }
        }
        return false;
   }

    @Override
    public void resetDirty(Object o, EntityPersister entityPersister, Session session) {
        if(canCast(o)){
            ((GsrsManualDirtyMaker)o).clearDirtyFields();
        }

    }

    @Override
    public void findDirty(Object o, EntityPersister entityPersister, Session session, DirtyCheckContext dirtyCheckContext) {
        if(canCast(o)) {

            Set<String> dirtyFields = ((GsrsManualDirtyMaker) o).getDirtyFields();
            if(!dirtyFields.isEmpty()){
                dirtyCheckContext.doDirtyChecking(attributeInformation -> dirtyFields.contains(attributeInformation.getName()));
            }
        }
   }
}
