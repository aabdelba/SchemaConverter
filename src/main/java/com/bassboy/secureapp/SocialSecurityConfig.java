package com.bassboy.secureapp;

import com.bassboy.services.SocialConnectionSignup;
import com.bassboy.services.SocialSignInAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.mem.InMemoryUsersConnectionRepository;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.connect.web.ProviderSignInController;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.google.api.Google;
import org.springframework.social.google.connect.GoogleConnectionFactory;
import org.springframework.social.oauth2.OAuth2Parameters;

@Configuration
@EnableWebSecurity
public class SocialSecurityConfig {

    @Autowired
    private SocialConnectionSignup socialConnectionSignup;

    @Value("${spring.social.google.appSecret}")
    String googleAppSecret;

    @Value("${spring.social.google.appId}")
    String googleAppId;

    @Value("${spring.social.google.scope}")
    String googleScope;

    @Value("${spring.social.facebook.appSecret}")
    String facebookAppSecret;

    @Value("${spring.social.facebook.appId}")
    String facebookAppId;

    @Value("${spring.social.facebook.scope}")
    String facebookScope;

    // config step 3
    // provide the general controller for social signIn rather than manually defining it
    @Bean
    public ProviderSignInController providerSignInController() {
        ConnectionFactoryLocator connectionFactoryLocator =
                connectionFactoryLocator();

        // create a repository from the one or more connection factories
        UsersConnectionRepository usersConnectionRepository =
                getUsersConnectionRepository(connectionFactoryLocator);




        GoogleConnectionFactory gFactory = (GoogleConnectionFactory) connectionFactoryLocator.getConnectionFactory("google");
        System.out.println(gFactory.getScope());

        FacebookConnectionFactory fbFactory = (FacebookConnectionFactory) connectionFactoryLocator.getConnectionFactory("facebook");
System.out.println(fbFactory.getScope());








        ((InMemoryUsersConnectionRepository) usersConnectionRepository)
                .setConnectionSignUp(socialConnectionSignup);

        return new ProviderSignInController(connectionFactoryLocator,
                usersConnectionRepository, new SocialSignInAdapter());
    }

    // config step 1
    private ConnectionFactoryLocator connectionFactoryLocator() {
        ConnectionFactoryRegistry registry = new ConnectionFactoryRegistry();

        GoogleConnectionFactory googleFactory = new GoogleConnectionFactory(googleAppId, googleAppSecret);
        googleFactory.setScope(googleScope);
        registry.addConnectionFactory(googleFactory);

        FacebookConnectionFactory facebookFactory = new FacebookConnectionFactory(facebookAppId, facebookAppSecret);
        facebookFactory.setScope(facebookScope);
        registry.addConnectionFactory(facebookFactory);

        return registry;
    }

    // config step 2
    private UsersConnectionRepository getUsersConnectionRepository(ConnectionFactoryLocator
                                                                           connectionFactoryLocator) {
        return new InMemoryUsersConnectionRepository(connectionFactoryLocator);
    }


}
