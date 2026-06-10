package app.product.project.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    String uploadFileToCloud(MultipartFile file);
}

