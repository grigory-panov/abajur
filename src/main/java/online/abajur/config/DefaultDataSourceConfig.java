package online.abajur.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

//@Configuration
//@EnableTransactionManagement
class DefaultDataSourceConfig implements DataSourceConfig {

    @Value("${backoffice.jdbc.url}")
    private String url;
    @Value("${backoffice.jdbc.user}")
    private String username;
    @Value("${backoffice.jdbc.password}")
    private String password;

    @Bean
    @Override
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setAutoCommit(false);
        ds.setMaximumPoolSize(15);
        ds.setMinimumIdle(1);
        ds.setDriverClassName("oracle.jdbc.driver.OracleDriver");
        ds.setRegisterMbeans(true);
        return ds;
    }

    @Bean
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

}
