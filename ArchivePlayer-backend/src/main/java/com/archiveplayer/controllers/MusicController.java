package com.archiveplayer.controllers;

import com.archiveplayer.services.MusicService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@RestController
@RequestMapping("/api/music")
public class MusicController {

    private final MusicService musicService;

    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    @GetMapping("/stream/{songId}")
    public ResponseEntity<StreamingResponseBody> streamSong(
            @PathVariable Long songId,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader) throws IOException {

        Path filePath = musicService.getSongFilePath(songId);
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        long fileSize = Files.size(filePath);
        String fileName = musicService.getFileName(songId);
        MediaType mediaType = MediaType.parseMediaType("audio/mpeg");

        if (rangeHeader == null) {
            StreamingResponseBody responseBody = outputStream -> {
                try (InputStream inputStream = Files.newInputStream(filePath)) {
                    inputStream.transferTo(outputStream);
                }
            };
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(fileSize)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .body(responseBody);
        } else {
            String[] ranges = rangeHeader.split("=")[1].split("-");
            long rangeStart = Long.parseLong(ranges[0]);
            long rangeEnd = fileSize - 1;

            if (ranges.length > 1 && !ranges[1].isEmpty()) {
                rangeEnd = Long.parseLong(ranges[1]);
            }

            if (rangeStart > fileSize || rangeEnd >= fileSize) {
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize)
                        .build();
            }

            long contentLength = rangeEnd - rangeStart + 1;

            StreamingResponseBody responseBody = outputStream -> {
                try (InputStream inputStream = Files.newInputStream(filePath)) {
                    inputStream.skip(rangeStart);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long bytesToRead = contentLength;
                    while (bytesToRead > 0 && (bytesRead = inputStream.read(buffer, 0, (int) Math.min(buffer.length, bytesToRead))) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        bytesToRead -= bytesRead;
                    }
                }
            };

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(mediaType)
                    .contentLength(contentLength)
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + rangeStart + "-" + rangeEnd + "/" + fileSize)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .body(responseBody);
        }
    }
}