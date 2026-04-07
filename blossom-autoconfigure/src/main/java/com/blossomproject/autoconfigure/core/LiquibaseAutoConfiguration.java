package com.blossomproject.autoconfigure.core;

import com.blossomproject.core.common.utils.liquibase.BlossomSpringLiquibase;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import java.time.Duration;
import java.util.function.Supplier;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseDataSource;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseProperties;
import org.springframework.boot.jpa.autoconfigure.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ResourceLoader;
import org.springframework.orm.jpa.AbstractEntityManagerFactoryBean;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

/**
 * Created by Maël Gargadennnec on 11/05/2017.
 */
@Configuration
public class LiquibaseAutoConfiguration {

  @Configuration
  @EnableConfigurationProperties({LiquibaseProperties.class, BlossomLiquibaseProperties.class})
  @Import(LiquibaseJpaDependencyConfiguration.class)
  public static class LiquibaseConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(LiquibaseConfiguration.class);

    private final LiquibaseProperties properties;

    private final DataSourceProperties dataSourceProperties;

    private final BlossomLiquibaseProperties blossomLiquibaseProperties;

    public LiquibaseConfiguration(
      LiquibaseProperties properties,
      DataSourceProperties dataSourceProperties,
      BlossomLiquibaseProperties blossomLiquibaseProperties) {
      this.properties = properties;
      this.dataSourceProperties = dataSourceProperties;
      this.blossomLiquibaseProperties = blossomLiquibaseProperties;
    }

    @Bean
    public SpringLiquibase liquibase(
      ObjectProvider<DataSource> dataSourceProvider,
      @LiquibaseDataSource ObjectProvider<DataSource> liquibaseDataSourceProvider, ResourceLoader resourceLoader) {
      DataSource dataSource = liquibaseDataSourceProvider.getIfAvailable();
      if (dataSource == null && properties.getUrl() != null && properties.getUser() != null) {
        dataSource = createLiquibaseDatasource();
      } else if (dataSource == null) {
        dataSource = dataSourceProvider.getIfUnique();
      }

      BlossomSpringLiquibase liquibase = new BlossomSpringLiquibase(resourceLoader);
      liquibase.setDataSource(dataSource);
      liquibase.setChangeLog(this.properties.getChangeLog());
      liquibase.setDefaultSchema(this.properties.getDefaultSchema());
      liquibase.setDropFirst(this.properties.isDropFirst());
      liquibase.setShouldRun(this.properties.isEnabled());
     
      liquibase.setChangeLogParameters(this.properties.getParameters());
      liquibase.setRollbackFile(this.properties.getRollbackFile());
      return liquibase;
    }

    private DataSource createLiquibaseDatasource() {
      String url = getPropertyOrDefault(properties::getUrl, dataSourceProperties::determineUrl);
      String user =
        getPropertyOrDefault(properties::getUser, dataSourceProperties::determineUsername);
      String password =
        getPropertyOrDefault(properties::getPassword, dataSourceProperties::determinePassword);
      DataSource dataSource =
        DataSourceBuilder.create().url(url).username(user).password(password).build();

      if (dataSource instanceof HikariDataSource) {
        ((HikariDataSource) dataSource)
          .setMaximumPoolSize(blossomLiquibaseProperties.getMaxPoolSize());
        if (blossomLiquibaseProperties.isCloseLiquibaseDatasource()) {
          ((HikariDataSource) dataSource).setIdleTimeout(Duration.ofSeconds(5).toMillis());
          ((HikariDataSource) dataSource).setMinimumIdle(0);
          ((HikariDataSource) dataSource).setMaxLifetime(Duration.ofSeconds(5).toMillis());
        }
      } else {
        logger.warn(
          "Liquibase datasource wasn't an instance of HikariDataSource. Blossom liquibase properties have been ignored.");
      }

      return dataSource;
    }

    private String getPropertyOrDefault(Supplier<String> property, Supplier<String> defaultValue) {
      return property.get() != null ? property.get() : defaultValue.get();
    }
  }

  @ConfigurationProperties("blossom.liquibase")
  public static class BlossomLiquibaseProperties {

    private boolean closeLiquibaseDatasource = false;

    private Integer maxPoolSize = 10;

    public boolean isCloseLiquibaseDatasource() {
      return closeLiquibaseDatasource;
    }

    public BlossomLiquibaseProperties setCloseLiquibaseDatasource(
      boolean closeLiquibaseDatasource) {
      this.closeLiquibaseDatasource = closeLiquibaseDatasource;
      return this;
    }

    public Integer getMaxPoolSize() {
      return maxPoolSize;
    }

    public BlossomLiquibaseProperties setMaxPoolSize(Integer maxPoolSize) {
      this.maxPoolSize = maxPoolSize;
      return this;
    }
  }

  /**
   * Additional configuration to ensure that {@link EntityManagerFactory} beans depend-on the liquibase bean.
   */
  @Configuration
  @ConditionalOnClass(LocalContainerEntityManagerFactoryBean.class)
  @ConditionalOnBean(AbstractEntityManagerFactoryBean.class)
  protected static class LiquibaseJpaDependencyConfiguration
    extends EntityManagerFactoryDependsOnPostProcessor {

    public LiquibaseJpaDependencyConfiguration() {
      super("liquibase");
    }
  }
}
