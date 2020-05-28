package com.bassboy.secureapp;

import com.bassboy.services.FacebookConnectionSignup;
import com.bassboy.services.FacebookSignInAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.mem.InMemoryUsersConnectionRepository;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.connect.web.ProviderSignInController;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;

@Configuration
@EnableWebSecurity
public class AppSecurityConfig extends WebSecurityConfigurerAdapter {

    //this is the service that interacts with User DAO class
    // just like we have:
    // Controller -> Service -> dao
    // we also have:
    // Configuration -> Service -> dao
    @Autowired
    private UserDetailsService userDetailsService;

    // use @Bean to indicate that this is a bean to be used in the web container
    @Bean
    public AuthenticationProvider authProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        provider.setUserDetailsService(userDetailsService);
//        provider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());//dont encode password, keep it in plain text
        provider.setPasswordEncoder(new BCryptPasswordEncoder());


        return provider;
    }

//    @Bean
//    @Override
//    protected UserDetailsService userDetailsService() {
//
//        // UserDetails and User is a built-in type in spring security
//        List<UserDetails> users = new ArrayList<>();
//        users.add(User.withDefaultPasswordEncoder().roles("USER").username("aabdelba").password("1234").build());
//
//        return new InMemoryUserDetailsManager(users);
//
//    }


    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/css/**","/js/**","/images/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()//cross-site reference to the default login page is disabled
            .authorizeRequests().antMatchers("/","/login*","/signin/**","/signup/**").permitAll()
            .anyRequest().authenticated()
            .and()//used to specify more properties
            .formLogin()
                .loginPage("/login").permitAll()
                .failureUrl("/login-error")
                .and()
            .logout().invalidateHttpSession(true)
            .clearAuthentication(true)//once you log out, you want to clear everything
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
            .logoutSuccessUrl("/logout-success").permitAll();
    }

}
