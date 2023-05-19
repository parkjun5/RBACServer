package com.roles.rbacserver.common.presentation;

import com.roles.rbacserver.login.application.JwtTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RightCheckControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JwtTokenService jwtTokenService;

    /**
     * <table>
     * <tr>
     * <th>ID</th>
     * <th>Role</th>
     * </tr>
     * <tr>
     * <td>A</td>
     * <td>SYSTEM_ADMIN, NORMAL_USER, LIMITED, STUDENT</td>
     * </tr>
     * <tr>
     * <td>B</td>
     * <td>NORMAL_USER</td>
     * </tr>
     * <tr>
     * <td>E</td>
     * <td>STUDENT</td>
     * </tr>
     * <tr>
     * <td>H</td>
     * <td>LIMITED</td>
     * </tr>
     * </table>
     */
    @Test
    @DisplayName("시스템 파일 수정 요청 성공")
    void editSystemFileAccessSuccessTest() throws Exception {
        //given
        String token = jwtTokenService.generateToken("A");

        //when
        MockHttpServletRequestBuilder requestBuilder = post("/api/check/system-file")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token);

        ResultActions resultActions = mockMvc.perform(requestBuilder).andDo(print());

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("statusCode").value(200))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("message").value("SYSTEM_ADMIN 만 접근 할 수 있습니다."));
    }

    @Test
    @DisplayName("시스템 파일 수정 요청 실패 > 권한 부족")
    void editSystemFileAccessFailTest() throws Exception {
        //given
        String token = jwtTokenService.generateToken("B");

        //when
        MockHttpServletRequestBuilder requestBuilder = post("/api/check/system-file")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token);

        ResultActions resultActions = mockMvc.perform(requestBuilder).andDo(print());

        //then
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("네트워크 접근 요청 성공")
    void accessNetworkSuccessTest() throws Exception {
        //given
        String token = jwtTokenService.generateToken("A");

        //when
        MockHttpServletRequestBuilder requestBuilder = get("/api/check/access/network")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token);

        ResultActions resultActions = mockMvc.perform(requestBuilder).andDo(print());

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("statusCode").value(200))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("message").value("SYSTEM_ADMIN, NORMAL_USER, STUDENT 만 접근 할 수 있습니다."));
    }

    @Test
    @DisplayName("네트워크 접근 요청 실패 > 권한 부족")
    void accessNetworkFailTest() throws Exception {
        //given
        String token = jwtTokenService.generateToken("H");

        //when
        MockHttpServletRequestBuilder requestBuilder = get("/api/check/access/network")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token);

        ResultActions resultActions = mockMvc.perform(requestBuilder).andDo(print());

        //then
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("사용자 파일 수정 요청 성공")
    void editUserFileAccessSuccessTest() throws Exception {
        //given
        String token = jwtTokenService.generateToken("A");

        //when
        MockHttpServletRequestBuilder requestBuilder = post("/api/check/user-file")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token);

        ResultActions resultActions = mockMvc.perform(requestBuilder).andDo(print());

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("statusCode").value(200))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("message").value("SYSTEM_ADMIN, NORMAL_USER, LIMITED 만 접근 할 수 있습니다."));
    }

    @Test
    @DisplayName("사용자 파일 수정 요청 실패 > 권한 부족")
    void editUserFileAccessFailTest() throws Exception {
        //given
        String token = jwtTokenService.generateToken("E");

        //when
        MockHttpServletRequestBuilder requestBuilder = post("/api/check/user-file")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token);

        ResultActions resultActions = mockMvc.perform(requestBuilder)
                .andDo(print());

        //then
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("일반 파일 읽기 요청 성공")
    void readFooBarFileSuccessTest() throws Exception {
        //given
        String token = jwtTokenService.generateToken("E");

        //when
        MockHttpServletRequestBuilder requestBuilder = get("/api/check/foo-bar-file")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token);

        ResultActions resultActions = mockMvc.perform(requestBuilder).andDo(print());

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("statusCode").value(200))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("message").value("SYSTEM_ADMIN, STUDENT 만 접근 할 수 있습니다."));
    }

    @Test
    @DisplayName("일반 파일 읽기 요청 실패 > 권한 부족")
    void readFooBarFileFailTest() throws Exception {
        //given
        String token = jwtTokenService.generateToken("B");

        //when
        MockHttpServletRequestBuilder requestBuilder = get("/api/check/foo-bar-file")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token);

        ResultActions resultActions = mockMvc.perform(requestBuilder).andDo(print());

        //then
        resultActions.andExpect(status().isForbidden());
    }

}