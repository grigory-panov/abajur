package online.abajur.service;

import online.abajur.repository.ImageRepository;
import online.abajur.repository.SettingsRepository;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
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
        try(InputStream origStream = new ByteArrayInputStream(content)){
            ImageIO.read(origStream); // throws IO Exception if not image
            Files.write(path, content);
        }
    }

    public List<String> getGalleryImagesPage(int page){
        return imageRepository.getGalleryImagesPage(page);
    }

    public int getGalleryPageCount(){
        return imageRepository.getGalleryPageCount();
    }
}
