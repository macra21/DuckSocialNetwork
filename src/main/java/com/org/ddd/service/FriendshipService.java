package com.org.ddd.service;

import com.org.ddd.domain.entities.Friendship;
import com.org.ddd.domain.entities.FriendshipStatus;
import com.org.ddd.domain.entities.User;
import com.org.ddd.domain.validation.exceptions.ValidationException;
import com.org.ddd.domain.validation.validators.Validator;
import com.org.ddd.dto.FriendshipFilterDTO;
import com.org.ddd.repository.AbstractRepository;
import com.org.ddd.repository.dbRepositories.FriendshipDBRepository;
import com.org.ddd.repository.exceptions.RepositoryException;
import com.org.ddd.utils.paging.Page;
import com.org.ddd.utils.paging.Pageable;

import java.util.*;

public class FriendshipService {
    private final FriendshipDBRepository friendshipRepository;
    private final AbstractRepository<Long, User> userRepository;
    private final Validator<Friendship> friendshipValidator;

    public FriendshipService(FriendshipDBRepository friendshipRepository, AbstractRepository<Long, User> userRepository, Validator<Friendship> friendshipValidator) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.friendshipValidator = friendshipValidator;
    }

    public void sendRequest(Long userId1, Long userId2) throws ValidationException, RepositoryException {
        Friendship newFriendship = new Friendship(userId1, userId2);
        newFriendship.setStatus(FriendshipStatus.PENDING);

        friendshipValidator.validate(newFriendship);
        businessRulesValidator(userId1, userId2);

        friendshipRepository.add(newFriendship);
    }

    public void acceptRequest(Long friendshipId) throws RepositoryException {
        Friendship friendship = friendshipRepository.findById(friendshipId);
        if (friendship == null) {
            throw new RepositoryException("Friendship request not found!");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new RepositoryException("Friendship is not in PENDING status!");
        }
        friendship.setStatus(FriendshipStatus.APPROVED);
        friendshipRepository.update(friendship);
    }

    public void rejectRequest(Long friendshipId) throws RepositoryException {
        Friendship friendship = friendshipRepository.findById(friendshipId);
        if (friendship == null) {
            throw new RepositoryException("Friendship request not found!");
        }
        friendship.setStatus(FriendshipStatus.REJECTED);
        friendshipRepository.update(friendship);
    }

    public void deleteFriendship(Long friendshipId) throws RepositoryException{
        Friendship friendshipToDelete = friendshipRepository.findById(friendshipId);

        if (friendshipToDelete == null)
            throw new RepositoryException("Friendship with id " + friendshipId + " does not exist!\n");

        friendshipRepository.delete(friendshipId);
    }

    public void deleteFriendship(Long userId1, Long userId2) throws RepositoryException{
        Friendship friendshipToDelete = findExistingFriendship(userId1, userId2);

        if (friendshipToDelete == null)
            throw new RepositoryException("Friendship between user " + userId1 + " and " + userId2 + " does not exist!\n");

        friendshipRepository.delete(friendshipToDelete.getId());
    }

    public Iterable<Friendship> getAllFriendships(){
        return friendshipRepository.findAll();
    }

    public Page<Friendship> getFriendshipsOnPage(Pageable pageable, FriendshipFilterDTO filter) {
        return friendshipRepository.findAllOnPage(pageable, filter);
    }

    private void businessRulesValidator(Long userId1, Long userId2) throws RepositoryException, ValidationException{
        userRepository.findById(userId1);
        userRepository.findById(userId2);

        if (findExistingFriendship(userId1, userId2) != null){
            throw new ValidationException("Friendship between user " + userId1 + " and " + userId2 + " already exists!\n");
        }
    }

    public Friendship findExistingFriendship(Long userId1, Long userId2){
        FriendshipFilterDTO f1 = new FriendshipFilterDTO();
        f1.setUserId1(userId1);
        f1.setUserId2(userId2);
        f1.setStatus(FriendshipStatus.APPROVED);
        Page<Friendship> p1 = friendshipRepository.findAllOnPage(new Pageable(0, 1), f1);
        if (p1.getTotalNumberOfElements() > 0) return (Friendship) p1.getElementsOnPage().iterator().next();

        FriendshipFilterDTO f2 = new FriendshipFilterDTO();
        f2.setUserId1(userId2);
        f2.setUserId2(userId1);
        f2.setStatus(FriendshipStatus.APPROVED);
        Page<Friendship> p2 = friendshipRepository.findAllOnPage(new Pageable(0, 1), f2);
        if (p2.getTotalNumberOfElements() > 0) return (Friendship) p2.getElementsOnPage().iterator().next();

        return null;
    }

    public Page<Friendship> findFriendshipsForUser(Long userId, Pageable pageable, FriendshipFilterDTO filter){
        userRepository.findById(userId);
        filter.setInvolvedUser(userId);
        return friendshipRepository.findAllOnPage(pageable, filter);
    }

    public void deleteAllFriendshipsForUser(Long userId) throws RepositoryException{
        Page<Friendship> friendshipsToDelete = findFriendshipsForUser(userId, new Pageable(0, 1000), new FriendshipFilterDTO());
        for (Friendship friendship: friendshipsToDelete.getElementsOnPage()){
            friendshipRepository.delete(friendship.getId());
        }
    }

    public int findNumberOfCommunities(){
        Map<Long, List<Long>> conections = new HashMap<>();
        for (User user: userRepository.findAll()){
            conections.put(user.getId(), new ArrayList<>());
        }

        for (Friendship friendship: friendshipRepository.findAll()){
            if (friendship.getStatus() == FriendshipStatus.APPROVED) {
                Long userId1 = friendship.getUserId1();
                Long userId2 = friendship.getUserId2();
                conections.get(userId1).add(userId2);
                conections.get(userId2).add(userId1);
            }
        }

        Map<Long, Boolean> visited = new HashMap<>();
        for (Long userId: conections.keySet()){
            visited.put(userId, false);
        }

        int communities = 0;
        for (Long userId: conections.keySet()){
            if (!visited.get(userId)){
                communities++;
                dfs(userId, conections, visited);
            }
        }

        return communities;
    }

    private void dfs(Long userId, Map<Long, List<Long>> conections, Map<Long, Boolean> visited) {
        visited.put(userId, true);
        for (Long friendId: conections.get(userId)){
            if (!visited.get(friendId)){
                dfs(friendId, conections, visited);
            }
        }
    }

    public Iterable<User> getMostSociableCommunity() {
        Map<Long, List<Long>> connections = new HashMap<>();
        for (User user : userRepository.findAll()) {
            connections.put(user.getId(), new ArrayList<>());
        }

        for (Friendship friendship : friendshipRepository.findAll()) {
            if (friendship.getStatus() == FriendshipStatus.APPROVED) {
                Long userId1 = friendship.getUserId1();
                Long userId2 = friendship.getUserId2();
                connections.get(userId1).add(userId2);
                connections.get(userId2).add(userId1);
            }
        }

        Map<Long, Boolean> visited = new HashMap<>();
        for (Long userId : connections.keySet()) {
            visited.put(userId, false);
        }

        List<List<Long>> communities = new ArrayList<>();
        for (Long userId : connections.keySet()) {
            if (!visited.get(userId)) {
                List<Long> community = new ArrayList<>();
                dfsCollect(userId, connections, visited, community);
                communities.add(community);
            }
        }

        int maxDiameter = -1;
        List<Long> mostSociableCommunity = new ArrayList<>();

        for (List<Long> community : communities) {
            int diameter = calculateDiameter(community, connections);
            if (diameter > maxDiameter) {
                maxDiameter = diameter;
                mostSociableCommunity = community;
            }
        }

        List<User> result = new ArrayList<>();
        for (Long userId : mostSociableCommunity) {
            result.add(userRepository.findById(userId));
        }

        return result;
    }

    private void dfsCollect(Long userId, Map<Long, List<Long>> connections,
                            Map<Long, Boolean> visited, List<Long> community) {
        visited.put(userId, true);
        community.add(userId);
        for (Long friendId : connections.get(userId)) {
            if (!visited.get(friendId)) {
                dfsCollect(friendId, connections, visited, community);
            }
        }
    }

    private int calculateDiameter(List<Long> community, Map<Long, List<Long>> connections) {
        if (community.size() <= 1) return 0;

        int maxDistance = 0;

        for (Long startNode : community) {
            int farthest = bfs(startNode, connections, community);
            maxDistance = Math.max(maxDistance, farthest);
        }

        return maxDistance;
    }

    private int bfs(Long start, Map<Long, List<Long>> connections, List<Long> community) {
        Map<Long, Integer> distances = new HashMap<>();
        for (Long userId : community) {
            distances.put(userId, -1);
        }

        Queue<Long> queue = new LinkedList<>();
        queue.add(start);
        distances.put(start, 0);

        int maxDistance = 0;

        while (!queue.isEmpty()) {
            Long current = queue.poll();
            int currentDist = distances.get(current);

            for (Long neighbor : connections.get(current)) {
                if (distances.containsKey(neighbor) && distances.get(neighbor) == -1) {
                    distances.put(neighbor, currentDist + 1);
                    queue.add(neighbor);
                    maxDistance = Math.max(maxDistance, currentDist + 1);
                }
            }
        }

        return maxDistance;
    }
}
