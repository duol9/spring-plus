package org.example.expert.domain.todo.dto.request;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TodoSearchRequest {
	private String keyword;
	private LocalDate startDay;
	private LocalDate endDay;
	private String managerName;
}
