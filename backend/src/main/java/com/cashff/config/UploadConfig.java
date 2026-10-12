package com.cashff.config;
import org.springframework.beans.factory.annotation.Value; import org.springframework.context.annotation.Configuration; import org.springframework.web.servlet.config.annotation.*; import java.nio.file.*;
@Configuration public class UploadConfig implements WebMvcConfigurer {
 private final String dir; public UploadConfig(@Value("${app.upload-dir:./uploads}") String dir){this.dir=dir;}
 @Override public void addResourceHandlers(ResourceHandlerRegistry registry){try{Path path=Paths.get(dir).toAbsolutePath().normalize();Files.createDirectories(path);registry.addResourceHandler("/uploads/**").addResourceLocations(path.toUri().toString());}catch(Exception ex){throw new IllegalStateException("Cannot initialize uploads directory",ex);}}
}
