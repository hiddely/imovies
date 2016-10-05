package ch.eth.infsec;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private DataSource dataSource;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/css/**").permitAll().anyRequest().permitAll();
        http.authorizeRequests().antMatchers("/js/**").permitAll().anyRequest().permitAll();
        http
                .authorizeRequests()
                .antMatchers("/", "/home").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
                .logout()
                .permitAll();

        // Configure X509 authentication
        // CN holds the username (in our case, email address)
        // This email address is then looked up in the database.
        // TODO: Something with revocation
        http.x509().subjectPrincipalRegex("CN=(.*?),");


    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        /*auth
                .inMemoryAuthentication()
                .withUser("user").password("password").roles("USER");*/

        auth
                .jdbcAuthentication()
                .dataSource(dataSource)
                .usersByUsernameQuery(getUserQuery())
                .authoritiesByUsernameQuery(getDefaultAuthorityQuery());
    }

    /**
     * Our database does not even support authorities. All users in the table have the normal USER role
     * @return authority query
     */
    private String getDefaultAuthorityQuery() {
        return "SELECT email as username, 'ROLE_USER' as authority "
                + "FROM users "
                + "WHERE email = ?";
    }

    private String getUserQuery() {
        return "SELECT email as username, pwd as password, true "
                + "FROM users "
                + "WHERE email = ?";
    }
}