package org.example.expert.domain.todo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.request.TodosGetRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor

// 트랜잭셔널 없앤 이유 - (나중에 커밋할 때 메시지에 쓸거임)
// @Transactional(readOnly = true) 해당 어노테이션의 의미는 읽기만 가능하다는 뜻으로. db 변화를 허용하지 않았음
// saveTodo는 db에 변화를 주는 동작을 하는 메서드로 readOnly에 걸리게 되어 오류가 발생했던 것
// 또한 클래스 전체에 @Transactional을 하는 것보단 각각의 메서드에 해주는 것이 좋다. 메서드마다 동작이 다르기 때문
// 결론적으로 조회하는 메서드인 getTodo, getTodos에는 @Transactional(readOnly = true)을 해주고,
// saveTodo에는 @Transactional(readOnly = false)로 변경해 읽기 전용을 비활성화했다.
public class TodoService {

    private final TodoRepository todoRepository;
    private final WeatherClient weatherClient;

    @Transactional
    public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
        User user = User.fromAuthUser(authUser);

        String weather = weatherClient.getTodayWeather();

        Todo newTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                weather,
                user
        );
        Todo savedTodo = todoRepository.save(newTodo);

        return new TodoSaveResponse(
                savedTodo.getId(),
                savedTodo.getTitle(),
                savedTodo.getContents(),
                weather,
                new UserResponse(user.getId(), user.getEmail())
        );
    }


    @Transactional(readOnly = true)
    public Page<TodoResponse> getTodos(int page, int size, TodosGetRequest todosGetRequest) {
        Pageable pageable = PageRequest.of(page - 1, size);

        LocalDateTime startDay = todosGetRequest.getStartDay() != null ? todosGetRequest.getStartDay().atStartOfDay() : null;
        LocalDateTime endDay = todosGetRequest.getEndDay() != null ? todosGetRequest.getEndDay().plusDays(1).atStartOfDay() : null;

        Page<Todo> todos = todoRepository.findAllByConditions(
            todosGetRequest.getWeather(),
            startDay,
            endDay,
            pageable);

        return todos.map(todo -> new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(todo.getUser().getId(), todo.getUser().getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        ));
    }

    @Transactional(readOnly = true)
    public TodoResponse getTodo(long todoId) {
        Todo todo = todoRepository.findByIdWithUser(todoId)
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));

        User user = todo.getUser();

        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(user.getId(), user.getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        );
    }
}
