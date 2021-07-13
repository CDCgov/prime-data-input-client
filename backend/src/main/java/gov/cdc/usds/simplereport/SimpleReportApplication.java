package gov.cdc.usds.simplereport;

import gov.cdc.usds.simplereport.config.AuthorizationProperties;
import gov.cdc.usds.simplereport.config.BeanProfiles;
import gov.cdc.usds.simplereport.config.CorsProperties;
import gov.cdc.usds.simplereport.config.InitialSetupProperties;
import gov.cdc.usds.simplereport.config.simplereport.DataHubConfig;
import gov.cdc.usds.simplereport.config.simplereport.DemoUserConfiguration;
import gov.cdc.usds.simplereport.properties.DynamicsProperties;
import gov.cdc.usds.simplereport.properties.ExperianProperties;
import gov.cdc.usds.simplereport.properties.OrderingProviderProperties;
import gov.cdc.usds.simplereport.properties.SendGridProperties;
import gov.cdc.usds.simplereport.properties.SmartyStreetsProperties;
import gov.cdc.usds.simplereport.service.OrganizationInitializingService;
import gov.cdc.usds.simplereport.service.ScheduledTasksService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
// Adding any configuration here should probably be added to SliceTestConfiguration
@EnableConfigurationProperties({
  InitialSetupProperties.class,
  AuthorizationProperties.class,
  DataHubConfig.class,
  DemoUserConfiguration.class,
  ExperianProperties.class,
  SmartyStreetsProperties.class,
  SendGridProperties.class,
  OrderingProviderProperties.class,
  DynamicsProperties.class,
  ExperianProperties.class,
  CorsProperties.class
})
@EnableAsync
@EnableScheduling
public class SimpleReportApplication {

  public static void main(String[] args) {
    SpringApplication.run(SimpleReportApplication.class, args);
  }

  @Bean
  @Profile(BeanProfiles.CREATE_SAMPLE_DATA)
  public CommandLineRunner initDataOnStartup(OrganizationInitializingService initService) {
    return args -> initService.initAll();
  }

  @Bean
  @ConditionalOnProperty("simple-report.data-hub.upload-enabled")
  public CommandLineRunner scheduleUploads(DataHubConfig config, ScheduledTasksService scheduler) {
    return args -> scheduler.scheduleUploads(config);
  }
}
