package online.abajur.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;


@Repository
public class SettingsRepository {

    private final JdbcTemplate template;
    public SettingsRepository(DataSource ds) {
        template = new JdbcTemplate(ds);
    }

    public static final String VERSION =  "1.0.2";

    public String getInviteCode() {
        return template.queryForObject("select val from settings where code = ?", String.class, "INVITE");
    }

    public String getStorageDir() {
        return template.queryForObject("select val from settings where code = ?", String.class, "STORAGE_DIR");
    }

    public int getTeamId() {
        return 9961;
    }

    public int getPageSize() {
        return Integer.parseInt(template.queryForObject("select val from settings where code = ?", String.class, "PAGE_SIZE"));
    }


}
