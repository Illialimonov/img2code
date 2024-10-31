package com.example.ilia.aidemo.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class GoogleStorageService {
    public String uploadFileToGCS(MultipartFile multipartFile) throws IOException {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        String originalFilename = multipartFile.getOriginalFilename();

        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IOException("The file's original filename is null or empty");
        }

        BlobId blobId = BlobId.of("photo2code_codes", originalFilename);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(multipartFile.getContentType()).build();

        // Upload the file
        storage.create(blobInfo, multipartFile.getBytes());
        System.out.println("File uploaded to GCS: " + originalFilename);

        return originalFilename;
    }

    public String uploadStringToGCS(String content, String filename) throws IOException {
        // Get the Storage service instance
        Storage storage = StorageOptions.getDefaultInstance().getService();

        if (filename == null || filename.isEmpty()) {
            throw new IOException("The filename is null or empty");
        }

        // Create a BlobId using the bucket name and the provided filename
        BlobId blobId = BlobId.of("photo2code_codes", filename);

        // Create BlobInfo with content type set to "text/plain" for a text file
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("text/plain") // or any other content type you need
                .build();

        // Upload the string content as a byte array
        storage.create(blobInfo, content.getBytes());

        System.out.println("String uploaded to GCS: " + filename);

        return filename;
    }

    public boolean deleteImageFromBucket(String fileName) {
        try {
            Storage storage = StorageOptions.getDefaultInstance().getService();
            BlobId blobId = BlobId.of("photo2code_codes", fileName);
            boolean deleted = storage.delete(blobId);

            if (deleted) {
                System.out.println("File " + fileName + " deleted from bucket " + "photo2code_codes");
                return true;
            } else {
                System.out.println("File " + fileName + " not found in bucket " + "photo2code_codes");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error deleting file: " + e.getMessage());
            return false;
        }
    }
}
