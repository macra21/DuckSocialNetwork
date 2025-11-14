package com.org.ddd.service;

import com.org.ddd.domain.entities.Duck;
import com.org.ddd.domain.entities.Flock;
import com.org.ddd.domain.entities.FlockPurpose;
import com.org.ddd.domain.entities.User;
import com.org.ddd.domain.validation.exceptions.ValidationException;
import com.org.ddd.domain.validation.validators.Validator;
import com.org.ddd.repository.AbstractRepository;
import com.org.ddd.repository.exceptions.RepositoryException;
import com.org.ddd.service.exceptions.ServiceException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FlockService {
    private final AbstractRepository<Long, Flock> flockRerpository;
    private final AbstractRepository<Long, User> userRepository;
    private final Validator<Flock> flockValidator;

    public FlockService(AbstractRepository<Long, Flock> flockRerpository, AbstractRepository<Long, User> userRepository, Validator<Flock> flockValidator) {
        this.flockRerpository = flockRerpository;
        this.userRepository = userRepository;
        this.flockValidator = flockValidator;
    }

    public Flock createFlock(String name, FlockPurpose purpose) throws ValidationException, RepositoryException {
        Flock newFlock = new Flock(name, purpose);
        flockValidator.validate(newFlock);
        flockRerpository.add(newFlock);
        return newFlock;
    }

    public Flock deleteFlock(Long flockId) throws RepositoryException{
        Flock flockToDelete = flockRerpository.findById(flockId);
        List<Long> memberIds = new ArrayList<>(flockToDelete.getMemberIds());
        for (Long duckId: memberIds){
            removeDuckFromFlock(duckId, flockId);
        }

        return flockRerpository.delete(flockId);
    }

    public Flock findById(Long flockId) throws RepositoryException{
        return flockRerpository.findById(flockId);
    }

    public Iterable<Flock> findAll(){
        return flockRerpository.findAll();
    }

    public void addDuckToFlock(Long duckId, Long flockId) throws ValidationException, RepositoryException, ServiceException{
        Flock flock = flockRerpository.findById(flockId);
        User user = userRepository.findById(duckId);

        if (!(user instanceof Duck)){
            throw new ServiceException("Only Ducks can be added to Flocks!\n");
        }
        Duck duck = (Duck) user;
        if (duck.getFlockId() != null || flock.hasMemberId(duckId)) {
            throw new ServiceException("Duck is already in a Flock(ID=" + duck.getFlockId() + ")!\n");
        }

        boolean isCompatible = false;
        if (flock.getFlockPurpose() == FlockPurpose.SwimMasters){
            isCompatible = duck.getDuckType().canSwim();
        }
        else if (flock.getFlockPurpose() == FlockPurpose.SkyFlyers){
            isCompatible = duck.getDuckType().canFly();
        }
        if (!isCompatible){
            throw new ServiceException("Duck type (" + duck.getDuckType().name() + ") is not compatible with Flock purpose (" + flock.getFlockPurpose().name() + ")!\n");
        }

        duck.setFlockId(flockId);
        userRepository.update(duck);

        flock.addMemberId(duckId);
        flockRerpository.update(flock);
    }

    public void removeDuckFromFlock(Long duckId, Long flockId) throws RepositoryException, ServiceException{
        Flock flock = flockRerpository.findById(flockId);
        User user = userRepository.findById(duckId);

        if (!(user instanceof Duck)){
            throw new ServiceException("Only Ducks can be removed from Flocks!\n");
        }
        Duck duck = (Duck) user;

        if (!Objects.equals(duck.getFlockId(), flockId)){
            throw new ServiceException("Duck is not in Flock(ID=" + flockId + ")!\n");
        }

        duck.setFlockId(null);
        userRepository.update(duck);

        flock.removeMemberId(duckId);
        flockRerpository.update(flock);
    }
}
