package com.example.reflix.web.controller;

import com.example.reflix.config.auth.userAdapter;
import com.example.reflix.service.*;
import com.example.reflix.web.dto.*;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Log4j2
public class ContentsController {

    private final ContentsCommonService contentsservice;
    private final AnimationServiceImpl animationService;
    private final MovieServiceImpl movieService;
    private final TvService tvService;
    private final YoutubeServiceImpl youtubeService;
    private final ReviewServiceImpl reviewService;

    private final String KEY ="cb3fbd26fe7fe53cf8af0ba2b6d72370";
    @ApiOperation(value = "콘텐츠취향을 분석하기",
            notes = "콘텐츠 종류, 장르, 키워드, 시간대 json으로 body에 담아서 요청," +
                    "영화 목록을 responsebody에 json으로 응답")
    @PostMapping("/contents/submit")
    public ResponseEntity contentSubmit(@RequestBody ContentsFavoriteRequestDto contentFavoriteDto, @AuthenticationPrincipal userAdapter user) throws IOException, InterruptedException {
//        log.info(user.getAuthorities());
        List<ContentsRecommendResponseDto> contentsList=new ArrayList<>();
        switch (contentFavoriteDto.getCategory().name()){
            case "ANIMATION": contentsList = animationService.submit(contentFavoriteDto,user); break;
            case "MOVIE" :  contentsList = movieService.submit(contentFavoriteDto,user);         log.info("contents id = "+(contentsList.get(0).getContentsId()));
                break;
            case "DRAMA" :  contentsList = tvService.submit(contentFavoriteDto,user); break;
        }
        log.info("contents id = "+(contentsList.get(0).getContentsId()));
//        List<filterContentsDto> contentsList = contentsservice.submit(contentFavoriteDto,user);//사용자식별아이디 추가해야됨
        if(contentsList.isEmpty()){
            String errMsg = "NO data";
            log.error(errMsg);
            return new ResponseEntity(errMsg, HttpStatus.NO_CONTENT);
        }
        else{
            return new ResponseEntity(contentsList, HttpStatus.OK);
        }
    }

    @ApiOperation(value = "필요한 x,y좌표, title을 해당위치 정보 db에 저장",
            notes = "필터링된 영화목록들 표시 ")
    @GetMapping("/contents/detail")
    public ResponseEntity contentdetail(@RequestParam Long contentId,@RequestParam int category){
        ContentsDetailResponseDto contents = null;
        switch (category){
            case 2: contents = animationService.contentdetail(contentId); break;
            case 0 :  contents = movieService.contentdetail(contentId); break;
            case 1 :  contents = tvService.contentdetail(contentId); break;
        }

//        contentsDetailResponseDto contents = contentsservice.contentdetail(contentId);

        if(contents==null){
            String errMsg="NO data";
            return new ResponseEntity(errMsg,HttpStatus.NO_CONTENT);
        }
        else{
            return new ResponseEntity(contents,HttpStatus.OK);
        }
        //그대로 리턴
        //왜 서브밋에서 바로 추천안하고 여기서 하냐
        //서브밋해서 필터링 단계와 보여주는 단계를 구분하기위해
        //dto 간단한 검증후 dto그대로 리턴하면 프론트에서 처리
    }

    @ApiOperation(value = "선택한 영화에 대한 리뷰영상 조회",
            notes = "해당영화의 이름을 url에 담아서 요청," +
                    "해당영화의 리뷰목록을 json으로 응답")
    @GetMapping("/contents/review")
    public ResponseEntity reviewlook(@RequestParam Long contentId,String contentname,int category){
        log.info(contentId);
        List<ReviewResponseDto> allReview = reviewService.reviewrecomend(contentId,category);
        if(allReview.isEmpty()){
            allReview = youtubeService.reviewStartSubmit(contentname,contentId,category);
        }
        if(allReview.isEmpty()){
            String errMsg= "No data";
            log.error("contentsController : reviewlook : "+errMsg);
            return new ResponseEntity(errMsg,HttpStatus.NO_CONTENT);
        }
        else{
            return new ResponseEntity(allReview,HttpStatus.OK);
        }
    }
    //유저의 리뷰시청목록에 추가
    @ApiOperation(value = "시청한 리뷰영상 히스토리에 저장",
            notes = "해당 유저의 email과 리뷰영상id를 바디에 담아서요청받음" +
                    "리뷰목록에 유저와 조인하여 저장")//시청한 리뷰영상목록에 저장
    @PostMapping("/review/look")
    public ResponseEntity reveiewSave(@RequestBody ReviewLookRequestDto reviewLookRequestDto , @AuthenticationPrincipal userAdapter userPrinciple){
        Long result = reviewService.reviewLookAdd(reviewLookRequestDto,userPrinciple);

        return new ResponseEntity(result,HttpStatus.OK);
    }

    @ApiOperation(value = "콘텐츠 좋아요 기능",
            notes = "콘텐츠 id와 유저 email을 바디에 담아서 요청받음" +
                    "콘텐츠테이블에 좋아요횟수 업데이트")
    @PostMapping("/content/like")
    public ResponseEntity contentLike(@RequestParam Long contentId,int category,int flag, @AuthenticationPrincipal userAdapter userPrinciple){
        if(flag==1){
            if(contentsservice.contentLike(contentId,category,userPrinciple)){
                log.info("상승");
                return new ResponseEntity(true, HttpStatus.OK);
            }
            else{
                log.info("상승실패 이유무엇");
                return new ResponseEntity(false,HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        else{
            if(contentsservice.contentDisLike(contentId, userPrinciple.getId())){
                log.info("하락");
                return new ResponseEntity(true, HttpStatus.OK);
            }
            else{
                log.info("하락실패 이유무엇");
                return new ResponseEntity(false,HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
    @GetMapping("/review/look/detail")
    public ResponseEntity reviewLookDetail(@AuthenticationPrincipal userAdapter user){
        return new ResponseEntity(reviewService.reviewLookDetail(user.getUser()),HttpStatus.OK);
    }


    @GetMapping("/contents/search")
    public ResponseEntity contentsSercrh(@RequestParam String q){

        List<ContentsDetailDto> resultList = new LinkedList<>();
        resultList.addAll(movieService.search(q));
        resultList.addAll(animationService.search(q));
        resultList.addAll(tvService.search(q));
        return new ResponseEntity(resultList,HttpStatus.OK);
    }
    @PostMapping("/auth/addmovie")
    public void movieadd(@RequestBody MovieAddDto dto){contentsservice.movieadd(dto);
    }



    @GetMapping("/auth/movie/test")
    public List<ContentsDetailDto> getTestMovie(){
        return movieService.getmovieten();
    }



    @GetMapping("/auth/api/getInfo")
    public String getInfo() {

        int pages = 1;

        try {

            for (int i = 1; i <= 1; i++) {
                String apiURL = "https://api.themoviedb.org/3/movie/11862?api_key=" + KEY
                        + "&language=ko";


//                String apiURL = "https://api.themoviedb.org/3/discover/movie?api_key=" + KEY
//                        + "&language=ko";
                URL url = new URL(apiURL);

                BufferedReader bf;

                bf = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));

                String result = bf.readLine();




                log.info(result);

//                ApiService.getInfo(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "ok";
    }
    @Scheduled(cron = "0 0 23 * * *", zone = "Asia/Seoul")
    public ResponseEntity movieUpdate(){
        //매일 23시마다 크롤링하여 영화정보들이 업데이트된다.
        //processbuilder를 통해 파이썬파일을 실행시켜 크롤링한다.
        //마지막은 리뷰클로링으로 끝내자
        String result = contentsservice.movieCrowling();
        if(result.equals("sucsess")){
            reviewUpdate();
            return new ResponseEntity("sucsess",HttpStatus.OK);
        }
        else if(result.contains("error : ")){
            log.info("contentsController - update error");
            return new ResponseEntity(result,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        else{
            return new ResponseEntity(result,HttpStatus.GATEWAY_TIMEOUT);
        }
    }


    public ResponseEntity reviewUpdate(){
        String result = contentsservice.reviewCrowling();
        if(result.equals("sucsess")){
            return new ResponseEntity("sucsess",HttpStatus.OK);
        }
        else if(result.contains("error : ")){
            return new ResponseEntity(result,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        else{
            return new ResponseEntity(result,HttpStatus.GATEWAY_TIMEOUT);
        }
    }
}