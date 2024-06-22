package de.lenneflow.orchestrationservice.feignclients;

import de.lenneflow.orchestrationservice.enums.RunNode;
import de.lenneflow.orchestrationservice.enums.TaskStatus;
import de.lenneflow.orchestrationservice.feignmodels.Task;

public class TaskServiceClientImpl {


    public Task getTask(String taskId) {
        Task task = new Task();
        task.setTaskDescription("Description");
        task.setTaskPriority(1);
        task.setRunNode(RunNode.WORKER);
        task.setTaskType("TypeA");
        task.setTaskStatus(TaskStatus.NOT_RUN);
        switch (taskId){
            case "t1":
                task.setTaskName("Task1");
                break;
            case "t2":
                task.setTaskName("Task2");
                break;
            case "t3":
                task.setTaskName("Task3");
                task.setTaskType("TypeB");
                break;
            case "t4":
                task.setTaskName("Task4");
                break;
            case "t5":
                task.setTaskName("Task5");
                task.setTaskType("TypeB");
                break;
            default:
                break;

        }
        return task;
    }
}
