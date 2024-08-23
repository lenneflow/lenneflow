package de.lenneflow.workerservice.util;

import de.lenneflow.workerservice.exception.InternalServiceException;
import de.lenneflow.workerservice.exception.PayloadNotValidException;
import de.lenneflow.workerservice.model.Worker;
import de.lenneflow.workerservice.repository.WorkerRepository;
import org.springframework.stereotype.Component;

@Component
public class PayloadValidator {

    final
    WorkerRepository workerRepository;

    public PayloadValidator(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    public void validate(Worker worker) {
        checkMandatoryFields(worker);

    }

    private void checkMandatoryFields(Worker worker) {
        if(worker.getName() == null || worker.getName().isEmpty()) {
            throw new PayloadNotValidException("Worker Name is required");
        }
        if(worker.getHostName() == null || worker.getHostName().isEmpty()) {
            throw new PayloadNotValidException("Host Name is required");
        }
        if(worker.getIpAddress() == null || worker.getIpAddress().isEmpty()) {
            throw new PayloadNotValidException("IP Address is required");
        }
        if(worker.getUid() == null || worker.getUid().isEmpty()) {
            throw new InternalServiceException("Uuid was not generated by the system");
        }
    }

}