package com.ecommerce.user.services;

import com.ecommerce.user.dto.AddressDTO;
import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.models.Address;
import com.ecommerce.user.models.User;
import com.ecommerce.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final KeyCloakAdminService keyCloakAdminService;

    public List<UserResponse> fetchAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public void addUser(UserRequest user) {
        String token = keyCloakAdminService.getAdminAccessToken();
        String keycloakUserId = keyCloakAdminService.createUser(token, user);

        User newUser = new User();
        updateUserFromRequest(newUser, user);
        newUser.setKeycloakId(keycloakUserId);

        keyCloakAdminService.assignRealmRoleToUser(user.getUsername(), "USER", keycloakUserId);
        userRepository.save(newUser);
    }

    public Optional<UserResponse> getUser(String id) {
        return userRepository.findById(id)
                .map(this::mapToUserResponse);
    }

    public UserResponse updateUser(String userId, UserRequest user) {
        // find user
        return userRepository.findById(userId)
                .map(existingUser -> {
                    // Update user
                    updateUserFromRequest(existingUser, user);
                    // Save user
                    userRepository.save(existingUser);
                    return existingUser;
                }).map(this::mapToUserResponse).orElse(null);
    }

    private void updateUserFromRequest(User newUser, UserRequest user) {
        newUser.setFirstName(user.getFirstName());
        newUser.setLastName(user.getLastName());
        newUser.setEmail(user.getEmail());
        newUser.setPhone(user.getPhone());
        if(user.getAddress() != null) {
            Address address = new Address();
            address.setStreet(user.getAddress().getStreet());
            address.setCity(user.getAddress().getCity());
            address.setState(user.getAddress().getState());
            address.setCountry(user.getAddress().getCountry());
            address.setZipcode(user.getAddress().getZipcode());

            newUser.setAddress(address);
        }
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setKeyCloakId(String.valueOf(user.getKeycloakId()));
        response.setId(String.valueOf(user.getId()));
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());

        if(user.getAddress() != null) {
            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setStreet(user.getAddress().getStreet());
            addressDTO.setCity(user.getAddress().getCity());
            addressDTO.setState(user.getAddress().getState());
            addressDTO.setCountry(user.getAddress().getCountry());
            addressDTO.setZipcode(user.getAddress().getZipcode());
            response.setAddress(addressDTO);
        }

        return response;
    }
}
