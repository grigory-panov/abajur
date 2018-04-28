package online.abajur.repository;

import org.springframework.stereotype.Repository;


@Repository
public class SettingsRepository {

    public String getInviteCode() {
        return "test";
    }

    public String getStorageDir() {
        return "/opt/storage/abajur";
    }

    public int getTeamId() {
        return 9961;
    }

    public int getPageSize() {
        return 50;
    }
}
