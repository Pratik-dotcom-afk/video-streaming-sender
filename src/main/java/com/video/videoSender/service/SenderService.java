package com.video.videoSender.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@Service
public class SenderService {

    @Value("${receiver.url}")
    private String receiverUrl;

    @Value("${video.source.path}")
    private String videoSourcePath;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendVideoChunks() {
        File videoFile = new File(videoSourcePath);
        if (!videoFile.exists()) {
            throw new RuntimeException("Video file not found at: " + videoSourcePath);
        }

        String fileExtension = getFileExtension(videoFile.getName());
        byte[] buffer = new byte[1024 * 1024]; // 1 MB chunks
        int bytesRead;
        int chunkNumber = 0;

        try (FileInputStream fis = new FileInputStream(videoFile)) {
            while ((bytesRead = fis.read(buffer)) > 0) {
                byte[] chunkData = new byte[bytesRead];
                System.arraycopy(buffer, 0, chunkData, 0, bytesRead);

                VideoChunk chunk = new VideoChunk(UUID.randomUUID().toString(), chunkNumber, chunkData, fileExtension);
                restTemplate.postForObject(receiverUrl + "/receiver/upload-chunk", chunk, Void.class);
                System.out.println("Sent chunk: " + chunkNumber);
                chunkNumber++;
            }
            restTemplate.postForObject(receiverUrl + "/receiver/all-chunks-sent", fileExtension, Void.class);
            System.out.println("All chunks sent successfully.");
        } catch (IOException e) {
            throw new RuntimeException("Error reading video file", e);
        }
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex > 0) ? fileName.substring(lastDotIndex + 1) : "";
    }

    public static class VideoChunk {
        private String id;
        private int sequence;
        private byte[] data;
        private String fileExtension;

        public VideoChunk() {}

        public VideoChunk(String id, int sequence, byte[] data, String fileExtension) {
            this.id = id;
            this.sequence = sequence;
            this.data = data;
            this.fileExtension = fileExtension;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getSequence() {
            return sequence;
        }

        public void setSequence(int sequence) {
            this.sequence = sequence;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        public String getFileExtension() {
            return fileExtension;
        }

        public void setFileExtension(String fileExtension) {
            this.fileExtension = fileExtension;
        }
    }
}
