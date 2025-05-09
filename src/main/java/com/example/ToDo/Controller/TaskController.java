package com.example.ToDo.Controller;

import com.example.ToDo.DTO.TaskRequest;
import com.example.ToDo.Exception.TaskNotFoundException;
import com.example.ToDo.Exception.UserNotFoundException;
import com.example.ToDo.Model.Task;
import com.example.ToDo.Model.User;
import com.example.ToDo.Notification.NotificationService;
import com.example.ToDo.Repository.TaskRepository;
import com.example.ToDo.Repository.UserRepository;
import com.example.ToDo.ServiceInterface.TaskService;
import com.example.ToDo.ServiceInterface.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Task Management", description = "API endpoints for managing tasks")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private static final Logger LOGGER = Logger.getLogger(TaskController.class.getName());
    private final TaskService taskService;
    private final UserService userService;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public TaskController(TaskService taskService,
                          UserService userService,
                          TaskRepository taskRepository,
                          UserRepository userRepository,
                          NotificationService notificationService) {
        this.taskService = taskService;
        this.userService = userService;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @PostMapping("/")
    @Operation(summary = "Create a new task", description = "Creates a new task for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Task.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Task> createTask(
            @RequestBody TaskRequest taskRequest,
            @Parameter(description = "JWT token with Bearer prefix", required = true)
            @RequestHeader("Authorization") String jwt) throws UserNotFoundException {
        LOGGER.info("Creating new task");

        User user = userService.findUserProfileByJwt(jwt);

        if (taskRequest == null || taskRequest.getTask() == null ||
                taskRequest.getUrgency() == null || taskRequest.getOperation() == null) {
            LOGGER.warning("Invalid task request: missing required fields");
            throw new IllegalArgumentException("Task, urgency, or operation must not be null or empty");
        }

        Task task = taskService.createTask(taskRequest);
        task.setUser(user);
        task = taskRepository.save(task);
        LOGGER.info("Task created with ID: " + task.getId());

        user.getTasks().add(task);
        userRepository.save(user);

        // Send notifications
        notificationService.sendEmailNotification(task, user, false);
        notificationService.sendSMSNotification(task, user, false);

        return new ResponseEntity<>(task, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Retrieves a specific task by its ID for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Task.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Task> getTaskById(
            @Parameter(description = "Task ID", required = true) @PathVariable Long id,
            @Parameter(description = "JWT token with Bearer prefix", required = true)
            @RequestHeader("Authorization") String jwt) throws TaskNotFoundException, UserNotFoundException {
        LOGGER.info("Fetching task with ID: " + id);

        User user = userService.findUserProfileByJwt(jwt);
        Task task = taskService.findTask(user.getTasks(), id, true);

        LOGGER.info("Task found: " + task.getId());
        return new ResponseEntity<>(task, HttpStatus.OK);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all tasks", description = "Retrieves all tasks for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Task.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Task>> getAllTasks(
            @Parameter(description = "JWT token with Bearer prefix", required = true)
            @RequestHeader("Authorization") String jwt) throws UserNotFoundException {
        LOGGER.info("Fetching all tasks for user");

        User user = userService.findUserProfileByJwt(jwt);
        List<Task> tasks = user.getTasks();

        LOGGER.info("Retrieved " + tasks.size() + " tasks");
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task", description = "Updates an existing task for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Task.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Task> updateTask(
            @Parameter(description = "Task ID", required = true) @PathVariable Long id,
            @RequestBody TaskRequest taskRequest,
            @Parameter(description = "JWT token with Bearer prefix", required = true)
            @RequestHeader("Authorization") String jwt) throws TaskNotFoundException, UserNotFoundException {
        LOGGER.info("Updating task with ID: " + id);

        if (taskRequest == null || taskRequest.getTask() == null ||
                taskRequest.getUrgency() == null || taskRequest.getOperation() == null) {
            LOGGER.warning("Invalid update request: missing required fields");
            throw new IllegalArgumentException("Task, urgency, or operation must not be null or empty");
        }

        User user = userService.findUserProfileByJwt(jwt);
        Task updatedTask = taskService.updateTask(id, taskRequest, user.getTasks());

        // Send notifications about the update
        notificationService.sendEmailNotification(updatedTask, user, true);
        notificationService.sendSMSNotification(updatedTask, user, true);

        LOGGER.info("Task updated: " + updatedTask.getId());
        return new ResponseEntity<>(updatedTask, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task", description = "Deletes a task by its ID for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task deleted successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> deleteTask(
            @Parameter(description = "Task ID", required = true) @PathVariable Long id,
            @Parameter(description = "JWT token with Bearer prefix", required = true)
            @RequestHeader("Authorization") String jwt) throws TaskNotFoundException, UserNotFoundException {
        LOGGER.info("Deleting task with ID: " + id);

        User user = userService.findUserProfileByJwt(jwt);
        Task task = taskService.findTask(user.getTasks(), id, true);

        user.getTasks().remove(task);
        taskService.deleteTask(task);
        userRepository.save(user);

        LOGGER.info("Task deleted successfully");
        return new ResponseEntity<>("Task deleted successfully", HttpStatus.OK);
    }
}