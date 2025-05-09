// TaskRequest.java
package com.example.ToDo.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Task request payload")
public class TaskRequest {
    @Schema(description = "Task operation type", example = "DEVELOPMENT", required = true)
    private String operation;

    @Schema(description = "Task description", example = "Complete the project", required = true)
    private String task;

    @Schema(description = "Task urgency level", example = "HIGH", required = true)
    private String urgency;

    @Schema(description = "Task scheduled date and time", example = "2025-05-10T15:30:00", required = false)
    private String dateTime;
}