package com.sparta.settlementservice.streaming.controller;


import com.sparta.settlementservice.streaming.dto.PlayRequest;
import com.sparta.settlementservice.streaming.service.StreamingService;
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
            , HttpServletRequest httpServletRequest
            , @RequestBody PlayRequest playRequest) {
        return streamingService.play(videoId, httpServletRequest, playRequest);
    }

    // 현재 재생 시점 db에 저장
    @PostMapping("/users/{userId}/videos/{videoId}/pause")
    public int pause(@PathVariable Long userId,
                      @PathVariable Long videoId,
                      @RequestParam Long currentPosition) {

        return streamingService.pause(userId, videoId, currentPosition);
    }



}
