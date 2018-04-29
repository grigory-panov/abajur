package online.abajur.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Repository
public class ImageRepository {

    @Autowired
    private SettingsRepository settingsRepository;

    private final JdbcTemplate template;

    public ImageRepository(DataSource ds) {
        template = new JdbcTemplate(ds);
    }

    @Transactional
    public void saveToGallery(String fileName) {
        Long id = template.queryForObject("select NEXT VALUE FOR SEQ_USER", Long.class);
        template.update("insert into gallery(id, uid) values(?, ?)", id, fileName);
    }

    @Transactional(readOnly = true)
    public List<String> getGalleryImagesPage(int page){

        int limit = settingsRepository.getPageSize();
        int offset = limit * (page-1);

        List<String> list = template.query("select uid from gallery order by id desc limit ? offset ?", (resultSet, i) -> resultSet.getString(1), limit, offset);
        return list;
    }

    public Path getFile(String fileName, ZonedDateTime now, String size) {
        if(!"full".equals(size) && !"medium".equals(size)){
            size = "medium";
        }
        return FileSystems.getDefault().getPath(settingsRepository.getStorageDir() + now.format(DateTimeFormatter.ofPattern("/yyyy/MM/")) + size, fileName);
    }

    @Transactional(readOnly = true)
    public int getGalleryPageCount() {
        int pageSize = settingsRepository.getPageSize();
        int count = template.queryForObject("select count(*) from gallery", Integer.class);
        return count / pageSize + (count % pageSize > 0 ? 1 : 0);
    }
}
