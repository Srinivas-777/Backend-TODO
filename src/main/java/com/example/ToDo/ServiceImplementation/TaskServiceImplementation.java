package com.example.ToDo.ServiceImplementation;

import com.example.ToDo.DTO.TaskRequest;
import com.example.ToDo.Exception.TaskNotFoundException;
import com.example.ToDo.Model.Task;
import com.example.ToDo.Repository.TaskRepository;
import com.example.ToDo.ServiceInterface.TaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskServiceImplementation implements TaskService {

    private final TaskRepository taskRepository;

    public TaskServiceImplementation(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public Task createTask(TaskRequest request) {
        Task task = new Task();
        task.setTask(request.getTask());
        task.setUrgency(request.getUrgency());
        task.setOperation(request.getOperation());
        task.setDateTime(request.getDateTime());
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return task;
    }

    @Override
    public Task updateTask(Long id, TaskRequest request, List<Task> userTasks) throws TaskNotFoundException {
        Task task = findTask(userTasks, id, true);

        task.setTask(request.getTask());
        task.setUrgency(request.getUrgency());
        task.setOperation(request.getOperation());
        task.setUpdatedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    @Override
    public void deleteTask(Task task) {
        taskRepository.delete(task);
    }

    @Override
    public Task findTask(List<Task> tasks, Long taskId, boolean throwExceptionIfNotFound) throws TaskNotFoundException {
        return tasks.stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> throwExceptionIfNotFound ? new TaskNotFoundException("Task not found with id: " + taskId) : null);
    }
}
