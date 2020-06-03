package com.bassboy.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
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
    // previously, bcrypt password encoder (or any other type) was used
    // in spring 2.0, we can delegate
    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // This is to use in-memory userDetails without using a database
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
    public void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()//security that stops cross-site reference to the app is disabled
            .authorizeRequests().antMatchers("/","/login*","/signin/**","/signup/**").permitAll()
            .anyRequest().authenticated()
            .and()
            .formLogin().loginPage("/login").permitAll().failureUrl("/login-error")
            .and()
            .logout().invalidateHttpSession(true)
            .clearAuthentication(true)
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
            .logoutSuccessUrl("/logout-success").permitAll()
            .and()//used to specify more properties
            .oauth2Login().loginPage("/login").failureUrl("/login-error").tokenEndpoint().accessTokenResponseClient(accessTokenResponseClient());
//            .formLogin()
//            .loginPage("/login").permitAll()
//            .failureUrl("/login-error")
//            .and()
//            .logout().invalidateHttpSession(true)
//            .clearAuthentication(true)//once you log out, you want to clear everything
//            .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
//            .logoutSuccessUrl("/logout-success").permitAll();
    }

    // AuthenticationManager has one-to-many AuthenticationProviders inside it
    // AuthenticationManager CAN have an optional parent AuthenticationProvider too
    // Configuring something in AuthenticationManager will make all its providers have these settings
    // If ProviderManager is used, this helps bypass using an AuthenticationManager
    // https://stackoverflow.com/questions/53404327/what-is-the-difference-between-registering-an-authenticationprovider-with-httpse
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    //    @Bean
//    public AuthenticationProvider authProvider(){
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//
//        provider.setUserDetailsService(userDetailsService);
////        provider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());//dont encode password, keep it in plain text
//        provider.setPasswordEncoder(new BCryptPasswordEncoder());
//
//
//        return provider;
//    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient(){
        DefaultAuthorizationCodeTokenResponseClient accessTokenResponseClient =
                new DefaultAuthorizationCodeTokenResponseClient();
//        accessTokenResponseClient.setRequestEntityConverter(new CustomRequestEntityConverter());

        OAuth2AccessTokenResponseHttpMessageConverter tokenResponseHttpMessageConverter =
                new OAuth2AccessTokenResponseHttpMessageConverter();
        tokenResponseHttpMessageConverter.setTokenResponseConverter(new LinkedinTokenResponseConverter());
        RestTemplate restTemplate = new RestTemplate(Arrays.asList(
                new FormHttpMessageConverter(), tokenResponseHttpMessageConverter));
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());

        accessTokenResponseClient.setRestOperations(restTemplate);
        return accessTokenResponseClient;
    }

}
