package com.bassboy.controllers;

import com.bassboy.common.ConfigProp;
import com.bassboy.models.SchemaEvolverUser;
import com.bassboy.services.SchemaEvolverUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ResolvableType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.WebAttributes;
import org.springframework.social.connect.Connection;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.NativeWebRequest;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController {

    @Value( " {custom.oauth2.baseUri} " )
    private String authorizationRequestBaseUri;

    // in-memory repository of oauth2 clients
    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private SchemaEvolverUserRepository repo;

    private RestTemplate restTemplate;

    @Autowired
    public void HelloController(RestTemplateBuilder builder){
        this.restTemplate = builder.build();
    }


//    @RequestMapping("/user")
//    @ResponseBody
//    public Principal user (Principal principal){
//        return principal;
//    }

    @RequestMapping("/login")
    public String getLogin(Model model) throws IOException, URISyntaxException {
        Iterable<ClientRegistration> clientRegistrations = null;
        ResolvableType type = ResolvableType.forInstance(clientRegistrationRepository)
                .as(Iterable.class);
        if (type != ResolvableType.NONE &&
                ClientRegistration.class.isAssignableFrom(type.resolveGenerics()[0])) {
            clientRegistrations = (Iterable<ClientRegistration>) clientRegistrationRepository;
        }

        clientRegistrations.forEach(registration ->
                model.addAttribute(registration.getClientName()+"Url", authorizationRequestBaseUri + "/" + registration.getRegistrationId()));

        Map<String, String> oauth2AuthenticationUrls = new HashMap<>();
        clientRegistrations.forEach(registration ->
                oauth2AuthenticationUrls.put(registration.getClientName(),
                        authorizationRequestBaseUri + "/" + registration.getRegistrationId()));


        model.addAttribute("urls", oauth2AuthenticationUrls);

        return "login";
    }

    @RequestMapping("/login-error")
    public String loginError(Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession(false);
        String errorMessage = null;
        if (session != null) {

            // get ${SPRING_SECURITY_LAST_EXCEPTION.message
            Exception ex = (Exception) session
                    .getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            if (ex != null) {
                errorMessage = ex.getMessage();
                if(ex.getClass().equals(BadCredentialsException.class)) {
                    model.addAttribute("errorMessage", errorMessage);
                } else {
                    ex.printStackTrace();

                    RequestDispatcher dispatcher = session.getServletContext()
                            .getRequestDispatcher("/error");
                    request.setAttribute("javax.servlet.error.message",ex.getMessage());
                    request.setAttribute("javax.servlet.error.status_code","500");
                    dispatcher.forward(request, response);
                }
            }

        }
        return "login";
    }

    @GetMapping("/logout-success")
    public String logout() {
        return "logout";
    }

    @GetMapping("/signup")
    public String getSignup() { return "signup"; }

    @PostMapping("/signup")
    public String postSignup(Model model, @RequestParam String username, @RequestParam String password, @RequestParam String email){
        model.addAttribute("usernameError", "errorMessage");
        model.addAttribute("passwordError", "errorMessage");
        model.addAttribute("emailError", "errorMessage");

        return "login";
    }

}
