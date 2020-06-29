package com.bassboy.controllers;

import com.bassboy.services.SchemaEvolverUserDetailsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ResolvableType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class LoginController {

    @Value( " {custom.oauth2.baseUri} " )
    private String authorizationRequestBaseUri;

    // in-memory repository of oauth2 clients
    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    SchemaEvolverUserDetailsService schemaEvolverUserDetailsService;

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
                if(ex.getClass().equals(BadCredentialsException.class)) {
                    model.addAttribute("errorMessage", ex.getMessage());
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

    @PostMapping("/signup-submission")
    public String postSignup(HttpServletRequest request, HttpServletResponse response, Model model,
                             @RequestParam String username, @RequestParam String password, @RequestParam String retypePassword,@RequestParam String email) throws ServletException, IOException {

        if(providedSignupInformationIsValid(model,username,email,password,retypePassword)){
            schemaEvolverUserDetailsService.createUser(username,email,password);
            return "signup-complete";
        }

        model.addAttribute("username", username);
        model.addAttribute("email", email);
        return "signup";
    }

    private Boolean providedSignupInformationIsValid(Model model, String username, String email, String password, String retypePassword) {
        Boolean signupValid = true;
        if(username.trim().equals("")){
            signupValid = false;
            model.addAttribute("usernameMessage", "Username was not provided");
        } else if(username.length()<3){
            signupValid = false;
            model.addAttribute("usernameMessage", "Username must be at least 3 characters long");
        } else if(schemaEvolverUserDetailsService.usernameExists(username)) {
            signupValid = false;
            model.addAttribute("usernameMessage", "Username already exists");
        }

        if(email.trim().equals("")){
            signupValid = false;
            model.addAttribute("emailMessage", "Email was not provided");
        } else if(StringUtils.countMatches(email,"@")!=1) {
            signupValid = false;
            model.addAttribute("emailMessage", "Invalid Email Address");
        } else if(schemaEvolverUserDetailsService.emailExists(email)) {
            signupValid = false;
            model.addAttribute("emailMessage", "Email already exists");
        } else {

            String emailSuffix = email.split("@")[0];
            String emailPrefix = email.split("@")[1];
            if(emailSuffix.length()<2
                    || emailPrefix.length()<4
                    || !emailPrefix.contains(".")
                    || emailSuffix.charAt(0)<65
                    || emailSuffix.charAt(0)>122
                    || (emailSuffix.charAt(0)<97 && emailSuffix.charAt(0)>90)) {
                signupValid = false;
                model.addAttribute("emailMessage", "Invalid Email Address");
            }
        }

        List<String> passwordMessages = new ArrayList<>();
        if(password.trim().equals("")){
            signupValid = false;
            passwordMessages.add("Password was not provided");
            model.addAttribute("passwordMessages", passwordMessages);
        } else {
            Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(password);
            boolean specialCharacterFound = m.find();

            if( password.equals(password.toLowerCase())
                    || password.equals(password.toUpperCase())
                    || password.length()<10
                    || !specialCharacterFound ){

                signupValid = false;
                passwordMessages.add("Password must:");
                passwordMessages.add("-be longer than 10 characters");
                passwordMessages.add("-contain at least one upper-case letter");
                passwordMessages.add("-contain at least one lower-case letter");
                passwordMessages.add("-contain at least one special character");
                model.addAttribute("passwordMessages", passwordMessages);
            } else if(retypePassword.trim().equals("")){
                signupValid = false;
                model.addAttribute("retypedPasswordMessage", "Re-typed password was not provided");
            } else if (!password.equals(retypePassword)){
                signupValid = false;
                model.addAttribute("retypePasswordMessage", "Passwords do not match");
            }
        }
        return signupValid;
    }

}
