package com.bassboy.controllers;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
public class UiErrorController implements ErrorController {

    @RequestMapping(value="error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        String errorTitle;

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
