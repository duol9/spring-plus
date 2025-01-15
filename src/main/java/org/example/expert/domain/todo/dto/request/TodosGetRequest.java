package org.example.expert.domain.todo.dto.request;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TodosGetRequest {
	private String weather;
	private LocalDate startDay;
	private LocalDate endDay;
}
