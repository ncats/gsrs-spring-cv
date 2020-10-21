package ix.core.models;

import com.fasterxml.jackson.annotation.JsonIgnore;



import javax.persistence.*;
import javax.validation.constraints.Email;

@Entity
@Table(name="ix_core_principal")
@Inheritance
@DiscriminatorValue("PRI")
public class Principal extends IxModel {
    // provider of this principal
    public String provider; 
    
   // @Required
    @Indexable(facet=true,name="Principal")
    @Column(unique=true)
    public String username;

    @Email
    public String email;
    
    @Column(name = "is_admin")
    public boolean admin = false;

    @Column(length=1024)
    public String uri; // can be email or any unique uri

    @ManyToOne(cascade = CascadeType.PERSIST)
    public Figure selfie;

    public Principal() {}
    
    public Principal(boolean admin) {
        this.admin = admin;
    }
    public Principal(String email) {
        this.email = email;
    }
    public Principal(boolean admin, String email) {
        this.admin = admin;
        this.email = email;
    }
    public Principal(String username, String email) {
        this.username = username;
        this.email = email;
    }
    
    @JsonIgnore
    public String toString(){
    	return username;
    }
    //TODO katzelda Octobe 2020 : don't think we need this userprofile factory call? its used in a few places in GSRS 2.x but in all cases we could use a repository instead?
    /*
    @JsonIgnore
    public UserProfile getUserProfile(){
    	return UserProfileFactory.getUserProfileForPrincipal(this);
    }
*/
    public boolean isAdmin () {
    	return admin; 
    }

    
}
