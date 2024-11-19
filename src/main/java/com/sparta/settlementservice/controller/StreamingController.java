package com.sparta.settlementservice.controller;


import com.sparta.settlementservice.dto.AdviewcountRequestDto;
import com.sparta.settlementservice.dto.PlayRequestDto;
import com.sparta.settlementservice.service.StreamingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class StreamingController {

    private final StreamingService streamingService;

    //조회 수 증가 후 현재 재생시점 반환
    @PostMapping("/videos/{videoId}/play")
    public int play(@PathVariable Long videoId
                    ,@RequestBody PlayRequestDto playRequestDto
                    ,HttpServletRequest httpServletRequest) {
              return streamingService.play(playRequestDto.getUserId(), videoId,httpServletRequest);
    }

    // 현재 재생 시점 db에 저장
    @GetMapping("/users/{userId}/videos/{videoId}/pause")
    public void pause(@PathVariable Long userId,
                      @PathVariable Long videoId,
                      @RequestParam int currentPosition) {

            streamingService.pause(userId,videoId,currentPosition);

    }

    //광고 조회수 증가
    @PostMapping("/adviewcount")
    public void adviewcount(@RequestBody AdviewcountRequestDto adviewcountRequestDto){
            streamingService.adviewcount(adviewcountRequestDto);
    }



}