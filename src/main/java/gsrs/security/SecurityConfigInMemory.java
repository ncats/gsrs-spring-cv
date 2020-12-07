package gsrs.security;

import ix.core.models.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true,
        proxyTargetClass = true,
        prePostEnabled = true)
@Configuration
public class SecurityConfigInMemory extends WebSecurityConfigurerAdapter {

    @Autowired
    LegacyGsrsAuthenticationProvider legacyGsrsAuthenticationProvider;

    @Autowired
    LegacyAuthenticationFilter legacyAuthenticationFilter;
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
        .antMatchers("/login*").permitAll()

        .anyRequest().permitAll()
//                .and().addFilterBefore(legacyAuthenticationFilter, BasicAuthenticationFilter.class)
                .and().authenticationProvider(legacyGsrsAuthenticationProvider)
//                .and().httpBasic()
//                .and().formLogin();
        .csrf().disable()

        ;
    }
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return NoOpPasswordEncoder.getInstance();
//    }
    @Bean
    public UserDetailsService userDetailsService() {

//        User.UserBuilder users = User.builder();
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(User.builder().username("user").password("{noop}password").roles(Role.Query.name()).build());
        manager.createUser(User.builder().username("admin").password("{noop}admin").roles( Role.Admin.name()).build());
        return manager;

    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

        auth.eraseCredentials(false);
        auth.userDetailsService(userDetailsService());
//        auth.inMemoryAuthentication().withUser("admin").password("{noop}admin").roles(Role.Admin.name());
//        auth.inMemoryAuthentication().withUser("user1").password("{noop}pass").roles(Role.Query.name());
//        auth.inMemoryAuthentication().withUser("user2").password("{noop}pass").roles(Role.SuperUpdate.name());
//        User.UserBuilder users = User.withDefaultPasswordEncoder();
//
//        auth.inMemoryAuthentication().wi("admin").password("admin").roles(Role.Admin.name());
//        auth.inMemoryAuthentication().withUser("user1").password("pass").roles(Role.Query.name());
//        auth.inMemoryAuthentication().withUser("user2").password("pass").roles(Role.SuperUpdate.name());

    }
}
