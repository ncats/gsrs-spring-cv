package gsrs.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class GsrsSecurityConfig extends WebSecurityConfigurerAdapter {

   @Bean
   public AuthenticationTrustResolver authenticationTrustResolver(){
       return new AuthenticationTrustResolverImpl();
   }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception
    {
        auth.parentAuthenticationManager(authenticationManagerBean())
        .userDetailsService(userDetailsService());
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/h2-console/**").permitAll()
                .and().csrf().ignoringAntMatchers("/h2-console/**")
                .and().headers().frameOptions().sameOrigin();

        http.csrf().disable();
//        http.authorizeRequests()
//                .antMatchers("/**")
//                .permitAll()
//                .and()
//                .httpBasic()
//        .and()
//        .formLogin();
    }
}
