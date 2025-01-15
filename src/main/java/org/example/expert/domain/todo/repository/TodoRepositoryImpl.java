package org.example.expert.domain.todo.repository;

import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

import java.util.Optional;

import org.example.expert.domain.todo.entity.Todo;

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
}
