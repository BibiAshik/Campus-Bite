package com.campusbite.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * ImageUploadService
 * Purpose: Handles saving uploaded food image files to the server's local filesystem.
 * Returns a URL path that can be stored in FoodItem.imageUrl and used directly in <img src="...">.
 */
@Service
public class ImageUploadService {

    private static final Logger log = LoggerFactory.getLogger(ImageUploadService.class);

    // Injected from application.properties — the folder path where images are saved
    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * Purpose: Saves an uploaded image file to the upload directory.
     * Input: MultipartFile — the image file from the admin's form upload.
     * Output: String — the URL path to store in FoodItem.imageUrl, e.g. "/uploads/food-images/abc123.jpg"
     * Throws: IOException if the file cannot be saved.
     */
    public String saveImage(MultipartFile file) throws IOException {

        // Step 1: Validate the file is not empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        // Step 2: Validate it is an image (only jpg, jpeg, png, webp allowed)
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("File has no name");
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!List.of(".jpg", ".jpeg", ".png", ".webp").contains(extension)) {
            throw new IllegalArgumentException("Only JPG, PNG, and WEBP images are allowed");
        }

        // Step 3: Generate a unique filename using UUID to avoid conflicts
        // e.g. "a3f9c812-4b2e-11ee-be56-0242ac120002.jpg"
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        // Step 4: Create the upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Step 5: Save the file to the upload directory
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Step 6: Return the URL path that will be stored in the database
        // This path is served as a static resource (configured in WebConfig)
        return "/uploads/food-images/" + uniqueFilename;
    }

    /**
     * Purpose: Deletes an old image file from the filesystem when a food item's image is replaced.
     * Input: imageUrl — the stored URL like "/uploads/food-images/abc123.jpg"
     *        Only deletes files in the uploads folder — never deletes the original seeded images.
     * Output: void
     */
    public void deleteImageIfUploaded(String imageUrl) {
        // Only delete files that are in the uploads folder, not the original seeded images
        if (imageUrl != null && imageUrl.startsWith("/uploads/food-images/")) {
            try {
                String filename = imageUrl.replace("/uploads/food-images/", "");
                Path filePath = Paths.get(uploadDir).resolve(filename);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Log but don't throw — a failed image delete should not crash the food item update
                log.warn("Could not delete old image file: {}", imageUrl);
            }
        }
    }
}
