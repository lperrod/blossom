package com.blossomproject.autoconfigure.core;

import com.blossomproject.core.cache.CacheConfig;
import com.blossomproject.core.cache.CacheConfig.CacheConfigBuilder;
import com.blossomproject.core.common.PluginConstants;
import com.blossomproject.core.common.dto.AbstractDTO;
import com.blossomproject.core.common.service.AssociationServicePlugin;
import com.blossomproject.core.common.utils.action_token.ActionTokenService;
import com.blossomproject.core.common.utils.mail.MailSender;
import com.blossomproject.core.user.User;
import com.blossomproject.core.user.UserDTO;
import com.blossomproject.core.user.UserDTOMapper;
import com.blossomproject.core.user.UserDao;
import com.blossomproject.core.user.UserDaoImpl;
import com.blossomproject.core.user.UserMailService;
import com.blossomproject.core.user.UserMailServiceImpl;
import com.blossomproject.core.user.UserRepository;
import com.blossomproject.core.user.UserService;
import com.blossomproject.core.user.UserServiceImpl;
import com.blossomproject.module.search.common.DefaultSearchEngineImpl;
import com.blossomproject.module.search.common.SearchEngineConfiguration;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Created by Maël Gargadennnec on 03/05/2017.
 */
@Configuration
@ConditionalOnClass(User.class)
@AutoConfigureAfter(CommonAutoConfiguration.class)
@EnableJpaRepositories(basePackageClasses = UserRepository.class)
@EntityScan(basePackageClasses = User.class)
public class UserAutoConfiguration {

  @Qualifier(PluginConstants.PLUGIN_ASSOCIATION_SERVICE)
  @Autowired
  private PluginRegistry<AssociationServicePlugin, Class<? extends AbstractDTO>> associationServicePlugins;


  @Bean
  @ConditionalOnMissingBean(UserMailService.class)
  public UserMailService userMailService(MailSender mailSender) {
    return new UserMailServiceImpl(mailSender);
  }

  @Bean
  @ConditionalOnMissingBean(UserService.class)
  public UserService userService(UserDao userDao, UserDTOMapper userDTOMapper,
    PasswordEncoder passwordEncoder, ActionTokenService actionTokenService,
    UserMailService userMailService,
    ApplicationEventPublisher eventPublisher,
    @Value("classpath:/images/avatar.jpeg") Resource defaultAvatarFile) throws IOException {
    if (!defaultAvatarFile.exists()) {
      throw new RuntimeException("Cannot find default user avatar on the classpath.");
    }
    return new UserServiceImpl(userDao, userDTOMapper, eventPublisher, associationServicePlugins,
      passwordEncoder,
      actionTokenService, userMailService,
      ByteStreams.toByteArray(defaultAvatarFile.getInputStream()));
  }

  @Bean
  @ConditionalOnMissingBean(UserDao.class)
  public UserDao userDao(UserRepository userRepository) {
    return new UserDaoImpl(userRepository);
  }

  @Bean
  @ConditionalOnMissingBean(UserDTOMapper.class)
  public UserDTOMapper userDTOMapper() {
    return new UserDTOMapper();
  }

  @Bean
  @ConditionalOnMissingBean(name = "userDaoCacheConfig")
  public CacheConfig userDaoCacheConfig() {
    return CacheConfigBuilder.create(UserDaoImpl.class.getCanonicalName())
      .specification("expireAfterWrite=15m").build();
  }

  @Bean
  public DefaultSearchEngineImpl<UserDTO> userDefaultSearchEngine(UserService userService,
    SearchEngineConfiguration<UserDTO> userSearchEngineConfiguration) {
    return new DefaultSearchEngineImpl<>(userSearchEngineConfiguration, userService);
  }

  @Bean
  public SearchEngineConfiguration<UserDTO> userSearchEngineConfiguration() {
    return new SearchEngineConfiguration<UserDTO>() {
      @Override
      public String getName() {
        return "menu.administration.users";
      }

      @Override
      public Class<UserDTO> getSupportedClass() {
        return UserDTO.class;
      }

      @Override
      public String[] getFields() {
        return new String[]{"dto.identifier", "dto.email", "dto.firstname", "dto.lastname",
          "dto.company", "dto.description", "dto.function"};
      }

      @Override
      public String getAlias() {
        return "users";
      }
    };
  }


}
