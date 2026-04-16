package com.priyansu.workflow.execution;

import com.priyansu.workflow.service.WorkflowExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AsyncWorkflowExecutor {

    private final WorkflowExecutionService workflowExecutionService;

//    @Async  //@Async → runs in separate thread, API thread is free immediately  , Executes workflow asynchronously in a separate thread. [ This improves API responsiveness and scalability for long-running tasks.]
//    public void execute(UUID workflowId){
//        workflowExecutionService.executeWorkflow(workflowId);
//    }
}
