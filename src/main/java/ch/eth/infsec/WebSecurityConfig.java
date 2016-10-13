package ch.eth.infsec;

import ch.eth.infsec.services.Sha1PasswordEncoder;
import ch.eth.infsec.services.UserDetailsServiceImpl;
import ch.eth.infsec.services.UserDetailsX509ServiceImpl;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/css/**").permitAll().anyRequest().permitAll();
        http.authorizeRequests().antMatchers("/js/**").permitAll().anyRequest().permitAll();
        http
                .authorizeRequests()
                .antMatchers("/", "/home").permitAll()
                .anyRequest().authenticated()
                .and()
                .x509().subjectPrincipalRegex("CN=(.*?),").userDetailsService(userDetailsX509Service())
                .and()
                .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/account", true)
                .permitAll()
                .and()
                .logout()
                .permitAll();

        // Configure X509 authentication
        // CN holds the username (in our case, email address)
        // This email address is then looked up in the database.
        // TODO: Something with revocation
        //http
        //        .antMatcher("/")
        //        .x509().subjectPrincipalRegex("CN=(.*?),").userDetailsService(userDetailsService());

    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        /*auth
                .inMemoryAuthentication()
                .withUser("user").password("password").roles("USER");*/

        auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }

    @Bean
    public UserDetailsService userDetailsX509Service() {
        return new UserDetailsX509ServiceImpl();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Sha1PasswordEncoder();
    }
}