package online.abajur.controller;

import online.abajur.AppException;
import online.abajur.repository.SettingsRepository;
import online.abajur.service.MozgvaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class LandingController {

    private static final Logger logger = LoggerFactory.getLogger(LandingController.class);

    @Autowired
    private MozgvaService mozgvaService;

    @Autowired
    private SettingsRepository settingsRepository;


    @RequestMapping(method = RequestMethod.GET, value = "/")
    public String getIndex(@RequestParam(name = "teamId", required = false) Integer teamId, Model model) throws AppException{
        if(teamId == null) {
            model.addAttribute("data", mozgvaService.getLandingPageData(settingsRepository.getTeamId()));
        }else{
            model.addAttribute("data", mozgvaService.getLandingPageData(teamId));
        }
        return "index";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/")
    public String postIndex(Model model){
        mozgvaService.clearCache();
        return "redirect:/";
    }

    @ExceptionHandler
    public ModelAndView exception(Exception exception, WebRequest request) {
        ModelAndView modelAndView = new ModelAndView("error");
        Throwable rootCause = exception.getCause() != null ? exception.getCause() : exception;
        modelAndView.addObject("errorMessage", rootCause);
        logger.error(rootCause.toString(), exception);
        modelAndView.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return modelAndView;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/error")
    public String getError(Model model) throws AppException{
        return "error";
    }
}
