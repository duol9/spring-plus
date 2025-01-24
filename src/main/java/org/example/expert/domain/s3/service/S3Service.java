package org.example.expert.domain.s3.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.UUID;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.response.UserImageResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.entity.UserProfileImage;
import org.example.expert.domain.user.repository.UserProfileRepository;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {
	private final AmazonS3 amazonS3;
	private final UserRepository userRepository;
	private final UserProfileRepository userProfileRepository;

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	// MultipartFile : HTTP 요청/응답과 관련된 파일 처리를 위한 스프링 프레임워크 (HTTP 요청 바디)
	// File은 로컬 파일 시스템의 파일을 처리
	@Transactional
	public UserImageResponse upload(AuthUser authUser, MultipartFile multipartFile, String dirName) throws IOException {
		long startTime = System.currentTimeMillis();

		User user = userRepository.findById(authUser.getId())
			.orElseThrow(() -> new IllegalArgumentException("유저가 없습니다."));

		// 원본 이름 가져옴
		String originalFileName = multipartFile.getOriginalFilename();

		// UUID(식별자)를 파일명에 추가
		String uuid = UUID.randomUUID().toString();
		String uniqueFileName = uuid + "_" + originalFileName.replaceAll("\\s","_");
		String fileName = dirName + "/" + uniqueFileName;

		try(BufferedInputStream bis = new BufferedInputStream(multipartFile.getInputStream())) {
			ObjectMetadata metadata = new ObjectMetadata(); // 파일의 추가적인 정보(크기, 콘텐츠 유형)를 설정
			metadata.setContentLength(multipartFile.getSize());
			metadata.setContentType(multipartFile.getContentType());

			String url = putS3(fileName, bis, metadata);

			UserProfileImage userProfileImage = new UserProfileImage(url, user);
			userProfileRepository.save(userProfileImage);

			long endTime = System.currentTimeMillis();
			log.info("Buffer 업로드 시간 : {}ms", endTime - startTime);
			return new UserImageResponse(url);
		}
	}

	private String putS3(String fileName, BufferedInputStream bis, ObjectMetadata metadata) {
		amazonS3.putObject(new PutObjectRequest(bucket, fileName, bis, metadata) // PutObjectRequest로 파일 생성 후 putObject로 업로드
			.withCannedAcl(CannedAccessControlList.PublicRead)); // 업로드 된 파일 접근 권한 설정
		return amazonS3.getUrl(bucket, fileName).toString(); // 저장된 URL 반환. 해당 URL로 이동 시 해당 파일 오픈
	}

	@Transactional
	public void deleteFile(String fileName) {
		try {
			UserProfileImage userProfileImage = userProfileRepository.findByImageUrl(fileName);
			userProfileRepository.delete(userProfileImage);
			// 원본 파일 명
			String decodedFileName = URLDecoder.decode(fileName, "UTF-8");
			log.info("삭제된 파일 : {}", decodedFileName);
			amazonS3.deleteObject(bucket, decodedFileName);
		} catch (UnsupportedEncodingException e) {
			log.error("에러 : {}", e.getMessage());
		}
	}

	public UserImageResponse updateFile(AuthUser authUser, MultipartFile newFile, String oldFileName,
		String dirName) throws IOException {
		// 기존 파일 S3에서 삭제
		deleteFile(oldFileName);
		// 새 파일 업로드
		return upload(authUser, newFile, dirName);
	}
}
