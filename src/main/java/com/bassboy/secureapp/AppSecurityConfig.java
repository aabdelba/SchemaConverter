package com.bassboy.secureapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.UserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;

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
            .authorizeRequests().antMatchers("/login","/").permitAll()// error creating bean springSecurityFilterChain. permitAll only works with HttpSecurity.authorizeRequests
            .anyRequest().authenticated()
            .and()//used to specify more properties
            .formLogin()
                .loginPage("/login").permitAll()
            .and()
            .logout().invalidateHttpSession(true)
            .clearAuthentication(true)//once you log out, you want to clear everything
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
            .logoutSuccessUrl("/").permitAll();
    }


}
