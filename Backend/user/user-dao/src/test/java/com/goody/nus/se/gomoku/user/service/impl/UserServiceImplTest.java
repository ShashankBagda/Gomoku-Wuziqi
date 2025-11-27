package com.goody.nus.se.gomoku.user.service.impl;

import com.goody.nus.se.gomoku.user.DbTestBase;
import com.goody.nus.se.gomoku.user.model.dto.UserDTO;
import com.goody.nus.se.gomoku.user.service.interfaces.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link UserServiceImpl}
 *
 * @author Haotian
 * @version 1.0, 2025/10/4
 */
class UserServiceImplTest extends DbTestBase {

    @Autowired
    private IUserService userService;

    @Test
    void save_validDTO_shouldReturnGeneratedId() {
        UserDTO dto = createTestUser("test@example.com", "testUser");

        Long userId = userService.save(dto);

        assertNotNull(userId);
        assertTrue(userId > 0);
    }

    @Test
    void save_nullDTO_shouldReturnNull() {
        Long result = userService.save(null);

        assertNull(result);
    }

    @Test
    void findById_existingUser_shouldReturnUser() {
        UserDTO savedUser = createAndSaveUser("find@example.com", "findUser");

        UserDTO found = userService.findById(savedUser.getId());

        assertNotNull(found);
        assertEquals(savedUser.getId(), found.getId());
        assertEquals(savedUser.getEmail(), found.getEmail());
        assertEquals(savedUser.getNickname(), found.getNickname());
    }

    @Test
    void findById_nonExistingUser_shouldReturnNull() {
        UserDTO result = userService.findById(999999L);

        assertNull(result);
    }

    @Test
    void findById_nullId_shouldReturnNull() {
        UserDTO result = userService.findById(null);

        assertNull(result);
    }

    @Test
    void findByEmail_existingUser_shouldReturnUser() {
        String email = "email@example.com";
        createAndSaveUser(email, "emailUser");

        UserDTO found = userService.findByEmail(email);

        assertNotNull(found);
        assertEquals(email, found.getEmail());
    }

    @Test
    void findByEmail_nonExistingEmail_shouldReturnNull() {
        UserDTO result = userService.findByEmail("nonexistent@example.com");

        assertNull(result);
    }

    @Test
    void findByEmail_nullEmail_shouldReturnNull() {
        UserDTO result = userService.findByEmail(null);

        assertNull(result);
    }

    @Test
    void findByEmail_emptyEmail_shouldReturnNull() {
        UserDTO result = userService.findByEmail("");

        assertNull(result);
    }

    @Test
    void findByNickname_existingUser_shouldReturnUser() {
        String nickname = "uniqueNickname";
        createAndSaveUser("nick@example.com", nickname);

        UserDTO found = userService.findByNickname(nickname);

        assertNotNull(found);
        assertEquals(nickname, found.getNickname());
    }

    @Test
    void findByNickname_nonExistingNickname_shouldReturnNull() {
        UserDTO result = userService.findByNickname("nonExistentNickname");

        assertNull(result);
    }

    @Test
    void findByNickname_nullNickname_shouldReturnNull() {
        UserDTO result = userService.findByNickname(null);

        assertNull(result);
    }

    @Test
    void existsByEmail_existingEmail_shouldReturnTrue() {
        String email = "exists@example.com";
        createAndSaveUser(email, "existsUser");

        boolean exists = userService.existsByEmail(email);

        assertTrue(exists);
    }

    @Test
    void existsByEmail_nonExistingEmail_shouldReturnFalse() {
        boolean exists = userService.existsByEmail("notexists@example.com");

        assertFalse(exists);
    }

    @Test
    void existsByEmail_nullEmail_shouldReturnFalse() {
        boolean exists = userService.existsByEmail(null);

        assertFalse(exists);
    }

    @Test
    void existsByNickname_existingNickname_shouldReturnTrue() {
        String nickname = "existingNick";
        createAndSaveUser("existnick@example.com", nickname);

        boolean exists = userService.existsByNickname(nickname);

        assertTrue(exists);
    }

    @Test
    void existsByNickname_nonExistingNickname_shouldReturnFalse() {
        boolean exists = userService.existsByNickname("nonExistingNick");

        assertFalse(exists);
    }

    @Test
    void existsByNickname_nullNickname_shouldReturnFalse() {
        boolean exists = userService.existsByNickname(null);

        assertFalse(exists);
    }

    @Test
    void update_existingUser_shouldUpdateSuccessfully() {
        UserDTO savedUser = createAndSaveUser("update@example.com", "updateUser");
        savedUser.setNickname("updatedNickname");
        savedUser.setCountry("US");

        int result = userService.update(savedUser);

        assertEquals(1, result);

        UserDTO updated = userService.findById(savedUser.getId());
        assertEquals("updatedNickname", updated.getNickname());
        assertEquals("US", updated.getCountry());
    }

    @Test
    void update_nullDTO_shouldReturnZero() {
        int result = userService.update(null);

        assertEquals(0, result);
    }

    @Test
    void deleteById_existingUser_shouldDeleteSuccessfully() {
        UserDTO savedUser = createAndSaveUser("delete@example.com", "deleteUser");

        int result = userService.deleteById(savedUser.getId());

        assertEquals(1, result);
        assertNull(userService.findById(savedUser.getId()));
    }

    @Test
    void deleteById_nonExistingId_shouldReturnZero() {
        int result = userService.deleteById(999999L);

        assertEquals(0, result);
    }

    @Test
    void deleteById_nullId_shouldReturnZero() {
        int result = userService.deleteById(null);

        assertEquals(0, result);
    }

    @Test
    void saveBatch_validList_shouldSaveAll() {
        UserDTO user1 = createTestUser("batch1@example.com", "batchUser1");
        UserDTO user2 = createTestUser("batch2@example.com", "batchUser2");
        List<UserDTO> users = Arrays.asList(user1, user2);

        int result = userService.saveBatch(users);

        assertEquals(2, result);
    }

    @Test
    void saveBatch_emptyList_shouldReturnZero() {
        int result = userService.saveBatch(Arrays.asList());

        assertEquals(0, result);
    }

    @Test
    void saveBatch_nullList_shouldReturnZero() {
        int result = userService.saveBatch(null);

        assertEquals(0, result);
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        createAndSaveUser("all1@example.com", "allUser1");
        createAndSaveUser("all2@example.com", "allUser2");

        List<UserDTO> allUsers = userService.findAll();

        assertNotNull(allUsers);
        assertTrue(allUsers.size() >= 2);
    }

    // Helper methods
    private UserDTO createTestUser(String email, String nickname) {
        return UserDTO.builder()
                .email(email)
                .nickname(nickname)
                .passwordHash("$2a$12$hashedPassword")
                .passwordSalt("salt123456789")
                .avatarUrl("")
                .avatarBase64("")
                .country("")
                .gender((byte) 0)
                .status((byte) 1)
                .build();
    }

    private UserDTO createAndSaveUser(String email, String nickname) {
        UserDTO dto = createTestUser(email, nickname);
        Long userId = userService.save(dto);
        dto.setId(userId);
        return dto;
    }
}
