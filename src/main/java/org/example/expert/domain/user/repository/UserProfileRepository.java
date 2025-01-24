package org.example.expert.domain.user.repository;

import org.example.expert.domain.user.entity.UserProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserProfileRepository extends JpaRepository<UserProfileImage, Long> {
	@Query ("SELECT upi " +
		"FROM UserProfileImage upi " +
		"WHERE upi.imageUrl = :imageUrl")
	UserProfileImage findByImageUrl (@Param("imageUrl") String imageUrl);
}
