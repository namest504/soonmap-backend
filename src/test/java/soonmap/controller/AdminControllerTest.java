package soonmap.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import soonmap.config.SecurityConfig;
import soonmap.dto.MemberDto.AdminLoginRequest;
import soonmap.entity.AccountType;
import soonmap.entity.Member;
import soonmap.exception.CustomException;
import soonmap.security.jwt.JwtProvider;
import soonmap.service.*;

import javax.servlet.http.Cookie;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

//@WebMvcTest(AdminController.class)
@WebMvcTest(controllers = AdminController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
        }
)
public class AdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    MemberService memberService;

    @MockBean
    JwtProvider jwtProvider;

    @MockBean
    S3Service s3Service;

    @MockBean
    NoticeService noticeService;

    @MockBean
    ArticleService articleService;

    @MockBean
    ArticleTypeService articleTypeService;


//    @MockBean
//    MemberPrincipal memberPrincipal;

    private AdminLoginRequest adminLoginRequest;
    private Member member;
    private Claims claims;

    @BeforeEach
    public void setUp() {
        adminLoginRequest = new AdminLoginRequest("test@email.com", "testPassword");
        member = new Member(1L, "testid1", "test@email.com", "test", "testPassword", AccountType.ADMIN, false, true, true, true, "testSnsId", LocalDateTime.now());
        claims = Jwts.claims();
    }

    @Test
    @DisplayName("어드민 로그인 403 반환 테스트")
    void isForbiddenAdminLogin() throws Exception {
        ResultActions resultActions = mockMvc.perform(post("/admin/login"));

        resultActions
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @DisplayName("어드민 로그인 401 반환 테스트")
    void isUnauthorizedAdminLogin() throws Exception {
        ResultActions resultActions = mockMvc.perform(post("/admin/login")
                .with(csrf()));

        resultActions
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @WithMockUser
    @DisplayName("어드민 로그인 성공 테스트")
    void successAdminLogin() throws Exception {
        // given
        given(memberService.loginAdmin(any())).willReturn(member);
        given(jwtProvider.createAccessToken(any())).willReturn("ACCESS_TOKEN");
        given(jwtProvider.createRefreshToken(any())).willReturn("REFRESH_TOKEN");

        ResponseCookie responseCookie = ResponseCookie.from("refreshToken", "REFRESH_TOKEN")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(3600000)
                .build();

        given(memberService.createHttpOnlyCookie(any())).willReturn(responseCookie);
        // when
        ResultActions resultActions = mockMvc.perform(post("/admin/login")
                .contentType("application/json")
                .with(csrf())
                .content(objectMapper.writeValueAsString(adminLoginRequest)));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String setCookieValue = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
                    assertThat(setCookieValue).contains(responseCookie.getValue());
                })
                .andExpect(header().string("Access-Token", "ACCESS_TOKEN"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.admin").value(true))
                .andExpect(jsonPath("$.manager").value(true))
                .andExpect(jsonPath("$.staff").value(true))
                .andDo(print());
    }

    @Test
    @WithMockUser
    @DisplayName("어드민 인증 받기 전 로그인 실패 테스트")
    void forbiddenAdminLogin() throws Exception {
        // given
        Member forbiddenMember = new Member(1L, "testid1", "test@email.com", "test", "testPassword", AccountType.ADMIN, true, true, true, true, "testSnsId", LocalDateTime.now());
        given(memberService.loginAdmin(any())).willThrow(new CustomException(HttpStatus.FORBIDDEN, "접근이 제한되었습니다."));

        // when
        ResultActions resultActions = mockMvc.perform(post("/admin/login")
                .contentType("application/json")
                .with(csrf())
                .content(objectMapper.writeValueAsString(adminLoginRequest)));

        // then
        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근이 제한되었습니다."))
                .andDo(print());

    }

    @Test
    @WithMockUser
    @DisplayName("어드민 refresh 성공 테스트")
    void successRefreshAdminToken() throws Exception {
        // given
        claims.setSubject("refresh_token");
        claims.put("uid", 1L);

        given(jwtProvider.decodeJwtToken("REFRESH_TOKEN")).willReturn(claims);
        given(memberService.findUserById(1L)).willReturn(member);
        given(memberService.getAdminRefreshToken(member.getId())).willReturn("REFRESH_TOKEN");
        given(jwtProvider.createAccessToken(member.getId())).willReturn("ACCESS_TOKEN");

        Cookie refreshTokenCookie = new Cookie("refreshToken", "REFRESH_TOKEN");

        // when
        ResultActions resultActions = mockMvc.perform(get("/admin/refresh")
                .cookie(refreshTokenCookie));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(content().string("true"))
                .andDo(print());
    }

    @Test
    @WithMockUser("ROLE_ADMIN")
    @DisplayName("어드민 계정 조회 성공 테스트")
    void successGetAdminAccount() throws Exception {
        // given
        Member adminAccount1 = new Member(1L, "testid1", "test1@email.com", "test1", "testPassword1", AccountType.ADMIN, false, true, true, true, "testSnsId", LocalDateTime.now());
        Member adminAccount2 = new Member(2L, "testid2", "test2@email.com", "test2", "testPassword2", AccountType.NAVER, false, false, false, true, "testSnsId", LocalDateTime.now());
        Member adminAccount3 = new Member(3L, "testid3", "test3@email.com", "test3", "testPassword3", AccountType.ADMIN, false, false, true, true, "testSnsId", LocalDateTime.now());
        List<Member> memberList = Arrays.asList(adminAccount1, adminAccount3);

        given(memberService.findAdminAccount()).willReturn(memberList);

        // when
        ResultActions resultActions = mockMvc.perform(get("/admin/account/admin"));

        // then

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountCount").value(2))
                .andExpect(jsonPath("$.memberList.[0]").exists())
                .andExpect(jsonPath("$.memberList.[1]").exists())
                .andDo(print());
    }

    @Test
    @WithMockUser("ROLE_ADMIN")
    @DisplayName("전체 계정 조회 성공 테스트")
    void successGetAllAccount() throws Exception {
        // given
        Member adminAccount1 = new Member(1L, "testid1", "test1@email.com", "test1", "testPassword1", AccountType.ADMIN, false, false, true, true, "testSnsId", LocalDateTime.now());
        Member adminAccount2 = new Member(2L, "testid2", "test2@email.com", "test2", "testPassword2", AccountType.ADMIN, false, true, true, true, "testSnsId", LocalDateTime.now());
        Member adminAccount3 = new Member(3L, "testid3", "test3@email.com", "test3", "testPassword3", AccountType.ADMIN, false, false, true, true, "testSnsId", LocalDateTime.now());
        List<Member> memberList = Arrays.asList(adminAccount1, adminAccount2, adminAccount3);
//        given(memberPrincipal.getMember().isAdmin()).willReturn(true);
        given(memberService.findAll()).willReturn(memberList);

        // when
        ResultActions resultActions = mockMvc.perform(get("/admin/account/all"));

        // then

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountCount").value(3))
                .andExpect(jsonPath("$.memberList.[0]").exists())
                .andExpect(jsonPath("$.memberList.[1]").exists())
                .andExpect(jsonPath("$.memberList.[2]").exists())
                .andDo(print());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("비인가된 유저의 어드민 계정 조회 실패 테스트")
    void unauthorizedGetAdminAccount() throws Exception {
        // given
        Member adminAccount1 = new Member(1L, "testid1", "test1@email.com", "test1", "testPassword1", AccountType.ADMIN, false, true, true, true, "testSnsId", LocalDateTime.now());
        Member adminAccount2 = new Member(2L, "testid2", "test2@email.com", "test2", "testPassword2", AccountType.ADMIN, false, true, true, true, "testSnsId", LocalDateTime.now());
        Member adminAccount3 = new Member(3L, "testid3", "test3@email.com", "test3", "testPassword3", AccountType.ADMIN, false, false, true, true, "testSnsId", LocalDateTime.now());
        List<Member> memberList = Arrays.asList(adminAccount1, adminAccount2, adminAccount3);
        given(memberService.findAdminAccount()).willReturn(memberList);

        // when
        ResultActions resultActions = mockMvc.perform(get("/admin/account"));

        // then

        resultActions
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("어드민 계정 조회 실패 테스트")
    void userGetAdminAccount() throws Exception {
        // given
        Member adminAccount1 = new Member(1L, "testid1", "test1@email.com", "test1", "testPassword1", AccountType.ADMIN, false, true, true, true, "testSnsId", LocalDateTime.now());
        Member adminAccount2 = new Member(2L, "testid2", "test2@email.com", "test2", "testPassword2", AccountType.ADMIN, false, true, true, true, "testSnsId", LocalDateTime.now());
        Member adminAccount3 = new Member(3L, "testid3", "test3@email.com", "test3", "testPassword3", AccountType.ADMIN, false, false, true, true, "testSnsId", LocalDateTime.now());
        List<Member> memberList = Arrays.asList(adminAccount1, adminAccount2, adminAccount3);
        given(memberService.findAdminAccount()).willReturn(memberList);

        // when
        ResultActions resultActions = mockMvc.perform(get("/admin/account/all"));

        // then

        resultActions
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }
}
