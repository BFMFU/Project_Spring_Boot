package app.product.project.service.impl;

import app.product.project.exception.CloudStorageException;
import app.product.project.exception.InvalidFileException;
import app.product.project.service.FileUploadService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    private final Cloudinary cloudinary;

    @Value("${file.upload.max-size:5242880}")  // Default 5MB
    private long maxFileSize;


    @Override
    public String uploadFileToCloud(MultipartFile file) {
        try {
            validateFile(file);

            log.info("Starting file upload to Cloudinary: {}", file.getOriginalFilename());

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "auto",
                            "folder", "cv_uploads",
                            "public_id", System.currentTimeMillis() + "_" + file.getOriginalFilename()
                    )
            );

            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("File uploaded successfully. Secure URL: {}", secureUrl);

            return secureUrl;

        } catch (InvalidFileException e) {
            log.error("Invalid file: {}", e.getMessage());
            throw e;
        } catch (IOException e) {
            log.error("Error reading file: {}", e.getMessage());
            throw new CloudStorageException("Lỗi đọc tệp tin: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error uploading file to Cloudinary: {}", e.getMessage(), e);
            throw new CloudStorageException("Lỗi tải tệp tin lên dịch vụ đám mây: " + e.getMessage(), e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Tệp tin không được để trống");
        }

        if (file.getSize() > maxFileSize) {
            throw new InvalidFileException(
                    String.format("Kích thước tệp tin vượt quá giới hạn %d MB",
                            maxFileSize / (1024 * 1024))
            );
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".pdf")) {
            throw new InvalidFileException("Chỉ chấp nhận tệp tin định dạng PDF");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new InvalidFileException("Loại tệp tin không hợp lệ. Yêu cầu: application/pdf");
        }
    }
}

