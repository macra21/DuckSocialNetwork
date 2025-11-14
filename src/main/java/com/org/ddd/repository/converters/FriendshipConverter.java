package com.org.ddd.repository.converters;

import com.org.ddd.domain.entities.Friendship;
import com.org.ddd.repository.exceptions.RepositoryException;

import java.time.LocalDateTime;

public class FriendshipConverter implements EntityConverter<Friendship>{
    private static final String SEPARATOR = ":::";
    @Override
    public String toLine(Friendship entity) {
        return entity.getId() + SEPARATOR +
                entity.getUserId1() + SEPARATOR +
                entity.getUserId2() + SEPARATOR +
                entity.getFriendsFrom().toString();
    }

    @Override
    public Friendship fromLine(String line) throws RepositoryException{
        String[] parts = line.split(SEPARATOR);

        if (parts.length != 4){
            throw new RepositoryException("Corrupt line or invalid format in file.\n");
        }

        Friendship friendship = new Friendship(
                Long.parseLong(parts[1]), // idUser1
                Long.parseLong(parts[2]), // idUser2
                LocalDateTime.parse(parts[3]) // friendsFrom
        );
        friendship.setId(Long.parseLong(parts[0]));
        return friendship;
    }

}
