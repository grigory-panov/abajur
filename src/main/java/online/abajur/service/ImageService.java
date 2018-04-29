package online.abajur.service;

import online.abajur.repository.ImageRepository;
import online.abajur.repository.SettingsRepository;
import org.apache.commons.io.IOUtils;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class ImageService {

    @Autowired
    private SettingsRepository settingsRepository;

    @Autowired
    private ImageRepository imageRepository;

    public String getNewFileName() {
        return  UUID.randomUUID().toString();
    }

    public void saveToGallery(String fileName) {
        imageRepository.saveToGallery(fileName);
    }

    public Path getFile(String fileName, ZonedDateTime now, String size) {
        return imageRepository.getFile(fileName, now, size);
    }

    public void scaleToMedium(String fileName, ZonedDateTime now) throws IOException {
        Path full = imageRepository.getFile(fileName, now, "full");
        Path medium = imageRepository.getFile(fileName, now, "medium");

        if(Files.notExists(medium.getParent())){
            Files.createDirectories(medium.getParent());
        }

        try(InputStream origStream = Files.newInputStream(full);
            OutputStream destStream = Files.newOutputStream(medium)){
            BufferedImage originalImage = ImageIO.read(origStream);
            BufferedImage destImage = Scalr.resize(originalImage, Scalr.Method.BALANCED,
                    Math.min(600, Math.min(originalImage.getWidth(), originalImage.getHeight())));
            ImageIO.write(destImage, "JPG", destStream);

        }

    }

    public void saveOriginalImage(String fileName, ZonedDateTime now, byte[] content) throws IOException {
        Path path = imageRepository.getFile(fileName, now, "full");
        if(Files.notExists(path.getParent())){
            Files.createDirectories(path.getParent());
        }
        Path full = getFile(fileName, now, "full");

        try(InputStream origStream = new ByteArrayInputStream(content);
            OutputStream destStream = Files.newOutputStream(full)){
            BufferedImage originalImage = ImageIO.read(origStream);
            ImageIO.write(originalImage, "JPG", destStream);

        }
    }

    public List<String> getGalleryImagesPage(int page){
        return imageRepository.getGalleryImagesPage(page);
    }

    public int getGalleryPageCount(){
        return imageRepository.getGalleryPageCount();
    }
}
