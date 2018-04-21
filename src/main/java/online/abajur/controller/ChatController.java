package online.abajur.controller;

import online.abajur.domain.AbajurUser;
import online.abajur.domain.ChatHistory;
import online.abajur.service.ChatService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatService chatService;

    @RequestMapping(value = "/chat.html", method = RequestMethod.POST)
    public String postCode(@RequestParam(name = "invite") String invite,
                           @RequestParam(name = "name") String name,
                           @RequestParam(name = "csrf") String csrf,
                           HttpSession session,
                           HttpServletResponse response,
                           Model model) {
        if (StringUtils.equals(csrf, (String) session.getAttribute("_csrf"))) {
            if (chatService.inviteValid(invite)) {
                UUID uid = UUID.randomUUID();
                chatService.saveUser(uid.toString(), name);
                Cookie cookie = new Cookie("abajur_user", uid.toString());
                cookie.setHttpOnly(true);
                cookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(365));// 1 year
                cookie.setPath("/");
                response.addCookie(cookie);
            } else {
                logger.error("invite fail");
            }
        } else {
            logger.error("csrf token fail");
        }
        return "redirect:/chat.html";
    }

    @RequestMapping(value = "/chat.html", method = RequestMethod.GET)
    public String getCahtPage(HttpSession session, HttpServletRequest request, Model model) {
        UUID csrf = UUID.randomUUID();
        session.setAttribute("_csrf", csrf.toString());
        model.addAttribute("csrf", csrf.toString());
        Cookie token = WebUtils.getCookie(request, "abajur_user");

        if (token != null) {
            AbajurUser user = chatService.getUser(token.getValue());
            if (user != null) {
                logger.info("cookie found, and user too {}", token.getValue());
                model.addAttribute("userId", user.getUid());
                model.addAttribute("userName", user.getName());
            } else {
                logger.info("cookie found {}, but user not", token.getValue());
            }
        }
        return "chat";
    }


    @RequestMapping(value = "/history", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ChatHistory getHistory(@RequestParam(name = "limit") int limit,
                                  @RequestParam(name = "offset") int offset,
                                  HttpServletRequest request){
        Cookie token = WebUtils.getCookie(request, "abajur_user");

        if (token != null) {
            AbajurUser user = chatService.getUser(token.getValue());
            if (user != null) {
                return chatService.getHistory(limit, offset);
            }
        }
        return null;
    }
}
