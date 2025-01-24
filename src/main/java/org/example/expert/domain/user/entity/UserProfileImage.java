package org.example.expert.domain.user.entity;

import org.example.expert.domain.common.entity.Timestamped;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "UserProfileImage")
@NoArgsConstructor
public class UserProfileImage extends Timestamped {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(columnDefinition = "TEXT")
	private String imageUrl;

	@OneToOne
	private User user;

	public UserProfileImage(String imageUrl, User user) {
		this.imageUrl = imageUrl;
		this.user = user;
	}
}
