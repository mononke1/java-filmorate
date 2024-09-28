package ru.yandex.practicum.filmorate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FriendController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FriendService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

@WebMvcTest(FriendController.class)
public class FriendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FriendService friendService;

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setLogin("testLogin");
        user.setEmail("test@mail.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));  // Дата рождения
    }

    @Test
    public void testAddFriend() throws Exception {
        when(friendService.addFriend(1L, 2L)).thenReturn(user);

        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    public void testRemoveFriend() throws Exception {
        when(friendService.removeFriend(1L, 2L)).thenReturn(user);

        mockMvc.perform(delete("/users/1/friends/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    public void testGetFriends() throws Exception {
        User friend1 = new User();
        friend1.setId(2L);
        friend1.setName("Friend 1");
        friend1.setEmail("friend1@mail.com");

        User friend2 = new User();
        friend2.setId(3L);
        friend2.setName("Friend 2");
        friend2.setEmail("friend2@mail.com");

        when(friendService.getFriends(1L)).thenReturn(Arrays.asList(friend1, friend2));

        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2L))
                .andExpect(jsonPath("$[0].name").value("Friend 1"))
                .andExpect(jsonPath("$[0].email").value("friend1@mail.com"))
                .andExpect(jsonPath("$[1].id").value(3L))
                .andExpect(jsonPath("$[1].name").value("Friend 2"))
                .andExpect(jsonPath("$[1].email").value("friend2@mail.com"));
    }

    @Test
    public void testGetCommonFriends() throws Exception {
        User commonFriend = new User();
        commonFriend.setId(3L);
        commonFriend.setName("Common Friend");
        commonFriend.setEmail("commonfriend@mail.com");

        when(friendService.getCommonFriends(1L, 2L)).thenReturn(Collections.singletonList(commonFriend));

        mockMvc.perform(get("/users/1/friends/common/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3L))
                .andExpect(jsonPath("$[0].name").value("Common Friend"))
                .andExpect(jsonPath("$[0].email").value("commonfriend@mail.com"));
    }
}
