package com.smalltalknow.service;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.smalltalknow.service.tool.EmailHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Date;

@RestController
@SpringBootApplication
public class SmallTalkApplication {
	private static final Logger logger = LoggerFactory.getLogger(SmallTalkApplication.class);

	public static void main(String[] args) throws UnirestException {
		EmailHelper.sendNewUserNotification("zjuwpn@gmail.com");
		SpringApplication.run(SmallTalkApplication.class, args);
	}

	@Bean
	public ServerEndpointExporter serverEndpointExporter(){
		return new ServerEndpointExporter();
	}

	@CrossOrigin
	public ResponseEntity<FileSystemResource> export(File file, String mimeType) {
		if (file == null) return null;

		HttpHeaders header = new HttpHeaders();
		header.add("Cache-Control", "no-cache, no-store, must-revalidate");
		header.add("Content-Disposition", "inline; filename=" + file.getName());
		header.add("Content-Type", mimeType);
		header.add("Pragma", "no-cache");
		header.add("Expires", "0");
		header.add("Last-Modified", new Date().toString());
		header.add("ETag", String.valueOf(System.currentTimeMillis()));

		return ResponseEntity.ok().headers(header).contentLength(file.length()).body(new FileSystemResource(file));
	}

	@RequestMapping(value = "/download", method = RequestMethod.GET)
	public ResponseEntity<FileSystemResource> fileDownload(
			@RequestParam(value = "id_unified", required = true) String fileIdStr,
			@RequestParam(value = "file_name", required = true) String fileName,
			@RequestParam(value = "mime_type", required = true) String mimeType
	) {
		// Windows
		String downloadPath = String.format("E:\\server\\download\\%s\\%s", fileIdStr, fileName);

		// Linux
		// String downloadPath = String.format("/server/download/%s/%s", fileIdStr, fileName);

		File file = new File(downloadPath);
		if (file.exists()) {
			return export(file, mimeType);
		}

		return null;
	}

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity fileUpload(
			@RequestParam(value = "id_unified", required = true) String fileIdStr,
			@RequestParam(value = "file_name", required = true) String fileName,
			@RequestParam(value = "mime_type", required = true) String mimeType,
			@RequestBody String fileRawContent
	) {
		logger.info("FILE STREAM IN -> " + fileIdStr);

		String base64Str = fileRawContent.substring(fileRawContent.indexOf(',') + 1, fileRawContent.length() - 1);

		// WINDOWS
		String uploadPath = String.format("E:\\server\\download\\%s\\%s", fileIdStr, fileName);

		// LINUX
		// String uploadPath = String.format("/server/download/%s/%s", fileIdStr, fileName);

		File file = new File(uploadPath);
		if (!file.exists())  {
			if (file.getParentFile().mkdir()) {
				try {
					if (file.createNewFile()) {
						OutputStream os = new FileOutputStream(file);
						os.write(Base64.getDecoder().decode(base64Str.getBytes()));
						os.close();
					}
					else {
						throw new RuntimeException("Create File Failed");
					}
				} catch (IOException e) {
					logger.info(e.getMessage());
				}
			} else {
				throw new RuntimeException("Create Directory Failed");
			}
		}

		// WebSocketController.fileMultiCast(fileIdStr);

		return ResponseEntity.ok().headers(new HttpHeaders()).body("{}");
	}
}
