package ix.core.models;


import com.fasterxml.jackson.annotation.JsonInclude;
import gov.nih.ncats.common.util.TimeUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


import javax.persistence.*;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class IxModel extends BaseModel {
    @Id
    @GeneratedValue //Ebean added GeneratedValue by default we have to be explicit in hibernate
    public Long id;
    @Version
    public Long version;


    @CreatedDate
    public Date created = TimeUtil.getCurrentDate();
    @LastModifiedDate
    public Date modified;
    public boolean deprecated;

    public IxModel() {}



	@Override
	public String fetchGlobalId() {
		if(id!=null)return this.getClass().getName() + ":" + id.toString();
		return null;
	}
}
