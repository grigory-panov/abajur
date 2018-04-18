package online.abajur.controller;

import online.abajur.domain.ImageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
public class GalleryController {

    private static final Logger logger = LoggerFactory.getLogger(GalleryController.class);
    @RequestMapping(method = RequestMethod.GET, value = "/gallery.html")
    public String getGalery(Model model, HttpServletRequest request){

        List<ImageInfo> images = new ArrayList<>();
        ImageInfo img1 = new ImageInfo();
        img1.setCssClass("col-sm-4");
        img1.setPath(request.getContextPath() + "/resources/img/abajur.jpg");
        img1.setOriginalPath(request.getContextPath() + "/resources/img/abajur.jpg");
        ImageInfo img2 = new ImageInfo();
        img2.setCssClass("col-sm-4");
        img2.setPath(request.getContextPath() + "/resources/img/abajur.jpg");
        img2.setOriginalPath(request.getContextPath() + "/resources/img/abajur.jpg");
        ImageInfo img3 = new ImageInfo();
        img3.setCssClass("col-sm-8");
        img3.setPath(request.getContextPath() + "/resources/img/abajur.jpg");
        img3.setOriginalPath(request.getContextPath() + "/resources/img/abajur.jpg");

        images.add(img1);
        images.add(img2);
        images.add(img3);
        model.addAttribute("images", images);
        return "gallery";
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
}
