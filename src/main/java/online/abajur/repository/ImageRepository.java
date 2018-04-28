package online.abajur.repository;

import online.abajur.domain.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Repository
public class ImageRepository {


    private static final List<String> storage = Collections.synchronizedList(new ArrayList<>());

    @Autowired
    private SettingsRepository settingsRepository;

    public void saveToGallery(String fileName) {
        storage.add(fileName);
    }

    public List<String> getGalleryImagesPage(int page){
        List<String> list = new ArrayList<>(storage);
        Collections.reverse(list);
        int limit = settingsRepository.getPageSize();
        int offset = limit * (page-1);
        int endInd = limit + offset;
        if(endInd >= storage.size()){
            endInd = storage.size();
        }
        if(endInd < offset){
            endInd = offset;
        }
        return list.subList(offset, endInd);
    }

    public Path getFile(String fileName, ZonedDateTime now, String size) {
        if(!"full".equals(size) && !"medium".equals(size)){
            size = "medium";
        }
        return FileSystems.getDefault().getPath(settingsRepository.getStorageDir() + now.format(DateTimeFormatter.ofPattern("/yyyy/MM/")) + size, fileName);
    }
}
