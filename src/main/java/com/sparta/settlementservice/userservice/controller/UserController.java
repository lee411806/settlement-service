package com.sparta.settlementservice.userservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.sparta.userservice.dto.SignupRequestDto;
import com.sparta.userservice.dto.UserInfoDto;
import com.sparta.userservice.entity.UserRoleEnum;
import com.sparta.userservice.jwt.JwtUtil;
import com.sparta.userservice.security.UserDetailsImpl;
import com.sparta.userservice.service.KakaoService;
import com.sparta.userservice.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final KakaoService kakaoService;

    @GetMapping("/user/login-page")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/user/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/user/signup")
    public String signup(@Valid SignupRequestDto requestDto, BindingResult bindingResult) {
        System.out.println("@PostMapping(\"/user/signup\")");
        // Validation 예외처리
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        if (fieldErrors.size() > 0) {
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                log.error(fieldError.getField() + " 필드 : " + fieldError.getDefaultMessage());
            }
            return "redirect:/api/user/signup";
        }

        userService.signup(requestDto);

        return "redirect:/api/user/login-page";
    }

    // 회원 관련 정보 받기
    @GetMapping("/user-info")
    @ResponseBody
    public UserInfoDto getUserInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        System.out.println(" @GetMapping(\"/user-info\")");
        String username = userDetails.getUser().getUsername();
        UserRoleEnum role = userDetails.getUser().getRole();
        boolean isAdmin = (role == UserRoleEnum.ADMIN);

        return new UserInfoDto(username, isAdmin);
    }

//    @GetMapping("/user-folder")
//    public String getUserInfo(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
//        model.addAttribute("folders", folderService.getFolders(userDetails.getUser()));
//
//        // #fragment : 따로 학습할 필요 없음 , 그냥 동적으로 보여지게 위해 표현해준거임
//        return "index :: #fragment";
//    }


    @GetMapping("/user/kakao/callback")
    public String kakaoLogin(@RequestParam String code, HttpServletResponse response) throws JsonProcessingException {
        System.out.println(" @GetMapping(\"/user/kakao/callback\")");
        String token = kakaoService.kakaoLogin(code);
        //bearer 다음에 공백이오는데 cookie에 공백이 오면 에러가 나기때문에 substring 해준다.
        Cookie cookie = new Cookie(JwtUtil.AUTHORIZATION_HEADER, token.substring(7));
        cookie.setPath("/");
        response.addCookie(cookie);

        return "redirect:/";
    }


}