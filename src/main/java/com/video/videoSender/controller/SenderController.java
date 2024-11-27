package com.video.videoSender.controller;

import com.video.videoSender.service.SenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SenderController {

    @Autowired
    private SenderService senderService;

    @GetMapping("/send-video")
    public String sendVideo() {
        try {
            senderService.sendVideoChunks();
            return "Video transfer initiated successfully.";
        } catch (Exception e) {
            return "Error during video transfer: " + e.getMessage();
        }
    }
}
