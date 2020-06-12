package com.bassboy.controllers;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
public class UiErrorController implements ErrorController {

    @RequestMapping(value="error", method = {RequestMethod.GET, RequestMethod.POST})
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        String errorTitle;

        if(message.toString().contains("user_cancelled_login"))
            return "index";

        if (status != null) {

            model.addAttribute("status",status);
            model.addAttribute("message",message);
            switch(status.toString()){
                case("500"):
                    errorTitle = " - Internal server error, our engineers are working on it";
//                    new Exception(message.toString()).printStackTrace();
                    model.addAttribute("message","");
                    break;
                case("400"):
                    errorTitle = " - Bad request";
                    break;
                case("404"):
                    errorTitle = " - Not found";
                    break;
                case("405"):
                    errorTitle = " - Method not allowed";
                    break;
                case("422"):
                    errorTitle = " - Unprocessable entity";
                    break;
                default:
                    errorTitle = "";
                    break;
            }
            model.addAttribute("errorTitle",errorTitle);

        }
        return "error";
    }


    @Override
    public String getErrorPath() {
        return "/error";
    }

}
