package com.blossomproject.demo;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
@EntityScan
public class DemoLauncher {

  public static void main(String[] args) {

    new SpringApplicationBuilder(DemoLauncher.class)
      .run(args);

  }
}
