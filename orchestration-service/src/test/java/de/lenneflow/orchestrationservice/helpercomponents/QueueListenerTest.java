package de.lenneflow.orchestrationservice.helpercomponents;

import de.lenneflow.orchestrationservice.dto.QueueElement;
import de.lenneflow.orchestrationservice.dto.ResultQueueElement;
import de.lenneflow.orchestrationservice.utils.Util;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.AmqpAdmin;

import java.io.IOException;

import static org.mockito.Mockito.*;

class QueueListenerTest {

    @Mock
    private AmqpAdmin admin;

    @Mock
    private WorkflowRunner workflowRunner;

    private QueueListener queueListener;

    MockedStatic<Util> utilities;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        queueListener = new QueueListener(admin, workflowRunner);
        utilities = Mockito.mockStatic(Util.class);
    }

    @AfterEach
    void tearDown() {
        utilities.close();
    }

    @Test
    void queueListener_processesQueueElement() {
        byte[] serializedElement = "serializedElement".getBytes();
        QueueElement queueElement = new QueueElement();
        utilities.when(() -> Util.deserializeQueueElement(serializedElement)).thenReturn(queueElement);

        queueListener.queueListener(serializedElement);

        verify(workflowRunner,  timeout(100).times(1)).processFunctionDtoFromQueue(queueElement);
    }

    @Test
    void queueListener_logsErrorOnException() {
        byte[] serializedElement = "serializedElement".getBytes();
        utilities.when(() -> Util.deserializeQueueElement(serializedElement)).thenThrow(new IOException("error"));

        queueListener.queueListener(serializedElement);

        verify(workflowRunner, never()).processFunctionDtoFromQueue(any());
    }

    @Test
    void resultQueueListener_processesResultQueueElement() {
        byte[] serializedElement = "serializedElement".getBytes();
        ResultQueueElement resultQueueElement = new ResultQueueElement();
        utilities.when(() -> Util.deserializeResultQueueElement(serializedElement)).thenReturn(resultQueueElement);

        queueListener.resultQueueListener(serializedElement);

        verify(workflowRunner).processResultFromQueue(resultQueueElement);
    }

    @Test
    void resultQueueListener_logsErrorOnException() {
        byte[] serializedElement = "serializedElement".getBytes();
        utilities.when(() -> Util.deserializeResultQueueElement(serializedElement)).thenThrow(new IOException("error"));

        queueListener.resultQueueListener(serializedElement);

        verify(workflowRunner, never()).processResultFromQueue(any());
    }
}