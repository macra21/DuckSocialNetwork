package com.org.ddd.service;

import com.org.ddd.domain.entities.Friendship;
import com.org.ddd.domain.entities.User;
import com.org.ddd.domain.validation.exceptions.ValidationException;
import com.org.ddd.domain.validation.validators.Validator;
import com.org.ddd.repository.AbstractRepository;
import com.org.ddd.repository.exceptions.RepositoryException;

import java.util.*;

public class FriendshipService {
    private final AbstractRepository<Long, Friendship> friendshipRepository;
    private final AbstractRepository<Long, User> userRepository;
    private final Validator<Friendship> friendshipValidator;

    public FriendshipService(AbstractRepository<Long, Friendship> friendshipRepository, AbstractRepository<Long, User> userRepository, Validator<Friendship> friendshipValidator) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.friendshipValidator = friendshipValidator;
    }

    public void addFriendship(Long userId1, Long userId2) throws ValidationException, RepositoryException{
        Friendship newFriendship = new Friendship(userId1, userId2);

        friendshipValidator.validate(newFriendship);

        businessRulesValidator(userId1, userId2);

        friendshipRepository.add(newFriendship);
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

    private void businessRulesValidator(Long userId1, Long userId2) throws RepositoryException, ValidationException{
        userRepository.findById(userId1);
        userRepository.findById(userId2);

        if (findExistingFriendship(userId1, userId2) != null){
            throw new ValidationException("Friendship between user " + userId1 + " and " + userId2 + " already exists!\n");
        }
    }

    private Friendship findExistingFriendship(Long userId1, Long userId2){
        for (Friendship friendship: friendshipRepository.findAll()){
            if (friendship.isBetween(userId1, userId2)){
                return friendship;
            }
        }
        return null;
    }

    public Iterable<Friendship> findFriendshipsForUser(Long userId){
        userRepository.findById(userId);

        List<Friendship> userFriendships = new ArrayList<>();

        for (Friendship friendship: friendshipRepository.findAll()){
            if (friendship.getUserId1().equals(userId) || friendship.getUserId2().equals(userId)){
                userFriendships.add(friendship);
            }
        }

        return userFriendships;
    }

    public void deleteAllFriendshipsForUser(Long userId) throws RepositoryException{
        List<Long> friendhipsToDelete = new ArrayList<>();
        for (Friendship friendship: friendshipRepository.findAll()){
            if (friendship.getUserId1().equals(userId) || friendship.getUserId2().equals(userId)){
                friendhipsToDelete.add(friendship.getId());
            }
        }
        for (Long friendshipId: friendhipsToDelete){
            friendshipRepository.delete(friendshipId);
        }
    }

    public int findNumberOfCommunities(){
        Map<Long, List<Long>> conections = new HashMap<>();
        for (User user: userRepository.findAll()){
            conections.put(user.getId(), new ArrayList<>());
        }

        for (Friendship friendship: friendshipRepository.findAll()){
            Long userId1 = friendship.getUserId1();
            Long userId2 = friendship.getUserId2();
            conections.get(userId1).add(userId2);
            conections.get(userId2).add(userId1);
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
            Long userId1 = friendship.getUserId1();
            Long userId2 = friendship.getUserId2();
            connections.get(userId1).add(userId2);
            connections.get(userId2).add(userId1);
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
