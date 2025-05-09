package com.example.ToDo.ServiceInterface;

import com.example.ToDo.DTO.TaskRequest;
import com.example.ToDo.Exception.TaskNotFoundException;
import com.example.ToDo.Model.Task;

import java.util.List;

public interface TaskService {
    Task createTask(TaskRequest request);
    Task updateTask(Long id, TaskRequest request, List<Task> userTasks) throws TaskNotFoundException;
    void deleteTask(Task task);
    Task findTask(List<Task> tasks, Long taskId, boolean throwExceptionIfNotFound) throws TaskNotFoundException;
}

