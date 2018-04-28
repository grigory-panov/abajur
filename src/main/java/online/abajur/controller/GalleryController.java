package online.abajur.controller;

import online.abajur.domain.AbajurUser;
import online.abajur.domain.ChatMessage;
import online.abajur.service.ChatService;
import online.abajur.service.ImageService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

@Controller
public class GalleryController {

    private static final Logger logger = LoggerFactory.getLogger(GalleryController.class);

    @Autowired
    private ChatService chatService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private SimpMessagingTemplate simpTemplate;

    @RequestMapping(method = RequestMethod.GET, value = "/gallery.html")
    public String getGalery(@RequestParam(name = "page", required = false) Integer page,
                            Model model, HttpServletRequest request){

        if(page == null || page <=0){
            page = 1;
        }
        model.addAttribute("images", imageService.getGalleryImagesPage(page));
        return "gallery";
    }


    @RequestMapping(method = RequestMethod.POST, value = "/sendfile", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, String> upload(@RequestParam(name = "text", required = false) String text,
                         @RequestParam(name = "toGalery", required = false) boolean toGalery,
                         @RequestParam(name = "file") MultipartFile formData,
                         Model model, HttpServletRequest request){
        Cookie token = WebUtils.getCookie(request, "abajur_user");

        if (token != null) {
            AbajurUser user = chatService.getUser(token.getValue());
            if (user != null) {
                String fileName = imageService.getNewFileName();
                ZonedDateTime now = ZonedDateTime.now();
                try {
                    imageService.saveOriginalImage(fileName, now, formData.getBytes());
                    imageService.scaleToMedium(fileName, now);

                } catch (IOException e) {
                    logger.error("cannot save image to file {} {}", e.getClass().getName(), e.getMessage());
                    return Collections.singletonMap("result", "ERROR");
                }
                ChatMessage cm = new ChatMessage();
                cm.setAuthor(user.getUid());
                cm.setAuthorName(user.getName());
                cm.setDate(now);
                cm.setFileId(fileName);
                cm.setText(text);
                cm.setType("file");
                chatService.saveMessage(cm);
                Map<String, Object> headers = new HashMap<>();
                headers.put("user-id", user.getUid());
                headers.put("user-name", user.getName());
                headers.put("type", "file");
                headers.put("msg-id", cm.getId());
                headers.put("file-id", fileName);
                if(toGalery){
                    imageService.saveToGallery(fileName);
                }
                simpTemplate.convertAndSend("/topic/teamChat",
                        text != null ? text.getBytes(UTF_8): "".getBytes(UTF_8),
                        headers);
                return Collections.singletonMap("result", "OK");
            }
        }
        return Collections.singletonMap("result", "ERROR");
    }


    @RequestMapping(method = RequestMethod.GET, value = "/storage/{id}/{size}", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public byte[] getImage(@PathVariable(name = "id") String uid,
                           @PathVariable(name = "size") String size,
                           Model model, HttpServletRequest request){
        ChatMessage cm = chatService.getMessageByImageId(uid);
        if(cm == null){
            logger.error("chat message not found for file {}", uid);
            return null;
        }

        try(InputStream stream = Files.newInputStream(imageService.getFile(uid, cm.getDate(), size))){
            return IOUtils.toByteArray(stream);
        }catch (Exception ex){
            logger.error("file {} not found: {} {}", uid, ex.getClass().getName(), ex.getMessage());
            return null;
        }



    }

}
