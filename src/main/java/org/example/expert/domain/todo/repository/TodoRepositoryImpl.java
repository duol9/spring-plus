package org.example.expert.domain.todo.repository;

import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;
import static org.example.expert.domain.comment.entity.QComment.comment;
import static org.example.expert.domain.manager.entity.QManager.manager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

public class TodoRepositoryImpl implements TodoRepositoryCustom {

	private final JPAQueryFactory queryFactory; // 동적 쿼리 작성하고 실행하기 위한 클래스

	public TodoRepositoryImpl(EntityManager entityManager) { // EntityManager는 엔티티를 관리하고 JPQL 쿼리 실행하는 역
		this.queryFactory = new JPAQueryFactory(entityManager); // JPAQueryFactory로 래핑해 QueryDSL 방식으로 작성 가능
	}

	@Override
	public Optional<Todo> findByIdWithUser(Long todoId) {
		return Optional.ofNullable(
			queryFactory
				.select(todo)
				.from(todo)
				.leftJoin(todo.user, user).fetchJoin() // fetchJoin()을 사용해 N+1 방지
				.where(todo.id.eq(todoId))
				.fetchOne() // 단일 데이터만 반환. 없으면 null 반환
		);
	}

	@Override
	public Page<TodoSearchResponse> findByCriteria(String keyword, LocalDateTime startDay, LocalDateTime endDay, String managerName,
		Pageable pageable) {

		List<TodoSearchResponse> todos = queryFactory
			.select(Projections.constructor(TodoSearchResponse.class,
				todo.title,
				todo.managers.size(),
				JPAExpressions.select(comment.count()) // 댓글은 개수만 알면 되는데 join을 굳이 해야할까,,? 서브쿼리로 하자
					.from(comment)
					.where(comment.todo.eq(todo))
			))
			.from(todo)
			.leftJoin(todo.managers, manager) // 매니저 이름으로 검색해야 하니까..
			.where (
				modifiedDaybetween(startDay, endDay),
				titleContains(keyword),
				managerNameContains(managerName)
			)
			.groupBy(todo.id) // id 기준으로 댓글과 매니저 묶어 그래야 카운팅 정확하게 됨
			.orderBy(todo.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> queryCount = queryFactory // 총 일정 개수 필요해.
			.select(todo.count())
			.from(todo)                          // 여기선 조인 안하는 이유는 어차피 개수만 알면 되니까.. 매핑한걸로 join이 내부적으로 돌긴 함..
			.where(
				modifiedDaybetween(startDay, endDay),
				titleContains(keyword),
				managerNameContains(managerName)
			);

		return PageableExecutionUtils.getPage(todos, pageable,queryCount::fetchOne);
	}

	// BooleanExpression -> null이면 무시
	private BooleanExpression modifiedDaybetween(LocalDateTime startDay, LocalDateTime endDay) {
		if (startDay != null && endDay != null) {
			return todo.createdAt.between(startDay, endDay);
		} else if (startDay != null) {
			return todo.createdAt.goe(startDay);
		} else if (endDay != null) {
			return todo.createdAt.loe(endDay);
		} else return null;
	}


	private BooleanExpression titleContains(String keyword) {
		return keyword != null ? todo.title.containsIgnoreCase(keyword) : null;
	}

	private BooleanExpression managerNameContains(String managerName) {
		return managerName != null ? todo.title.containsIgnoreCase(managerName) : null;
	}
}
