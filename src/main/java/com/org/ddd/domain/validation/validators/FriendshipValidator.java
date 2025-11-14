package com.org.ddd.domain.validation.validators;

import com.org.ddd.domain.entities.Friendship;
import com.org.ddd.domain.validation.exceptions.ValidationException;

import java.util.Objects;

public class FriendshipValidator implements Validator<Friendship> {
    @Override
    public void validate(Friendship friendship) {
        StringBuilder errors = new StringBuilder();

        if (friendship.getUserId1() == null){
            errors.append("ID of the first user cannot be null!\n");
        }
        if (friendship.getUserId2() == null){
            errors.append("ID of the second user cannot be null!\n");
        }
        if (Objects.equals(friendship.getUserId1(), friendship.getUserId2())){
            errors.append("Users cannot be friends with themselves!\n");
        }

        if (!errors.isEmpty())
            throw new ValidationException(errors.toString());
    }
}
