package org.example.expert.domain.manager.service;

import org.example.expert.domain.manager.entity.Log;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.LogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogService {
	private final LogRepository logRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveLog(Manager manager, Boolean isRegistration) {
		Log log = new Log(manager, isRegistration);
		logRepository.save(log);
	}
}
