package ppeonfun.controller.user.community;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import ppeonfun.dto.Community;
import ppeonfun.dto.CommunityJoin;
import ppeonfun.dto.Favorite;
import ppeonfun.dto.Information;
import ppeonfun.dto.News;
import ppeonfun.dto.Supporter;
import ppeonfun.dto.SupporterJoin;
import ppeonfun.service.user.community.CommunityService;

@Controller("user.CommunityController")
public class CommunityController {
	
	private static final Logger logger = LoggerFactory.getLogger(CommunityController.class);
	
	@Autowired CommunityService communityService;
	
	
	@RequestMapping(value = "/community", method = RequestMethod.GET)
	public String community(
			Model model, Information info, Supporter supporter, SupporterJoin suJoin, 
			News news, HttpSession session, CommunityJoin communityJoin ) {
		
		//해당 프로젝트의 제목, 카테고리, 목표금액
		info = communityService.projectInfo(info);
		
		//해당 프로젝트의 총 서포터 수 구하기
		int totalCount = communityService.totalCount(supporter);
		
		//프로젝트 남은 일수 구하기
		int remaining_day = communityService.remainDay(suJoin);
		
		//프로젝트의 총 펀딩 액
		int total_amount = communityService.amount(suJoin);
		
		//새소식 개수
		int newsCount = communityService.newsCount(news);
		
		//회원의 찜 상태 조회
		Favorite favorite = new Favorite();
		favorite.setpNo(news.getpNo()); //프로젝트 번호
		
		//로그인 되어있을 경우
		if(session.getAttribute("mNo") != null) {
		
			favorite.setmNo(((Integer)session.getAttribute("mNo")).intValue()); //회원번호
			
			//회원의 찜 상태 확인
			boolean isFav = communityService.isFav(favorite); 
//			logger.info("찜 상태 확인 {}", isFav);
			
			model.addAttribute("isFav", isFav); //회원의 찜 상태 전달
			
		} else {  //로그인 안되어있을 경우
			model.addAttribute("isFav", false); //회원의 찜 상태 전달
		}
		
		model.addAttribute("info", info);
		model.addAttribute("totalCnt", totalCount);
		model.addAttribute("remainDay", remaining_day);
		model.addAttribute("totalAmount", total_amount);
		model.addAttribute("newsCnt", newsCount);

		model.addAttribute("cntFav", communityService.getTotalCntFavorite(favorite)); //총 좋아요 횟수
		
		//----------------------------------------------------------------------------------------
		
//		logger.info("{}", communityJoin);
		
		//커뮤니티 질문,답변 리스트
		List<CommunityJoin> list = communityService.commuList(communityJoin);
		logger.info("커뮤니티 질문 리스트 {}", list);
		
		model.addAttribute("list", list);
		
		return "/user/project/community";
	}
	
	
	@RequestMapping(value = "/community", method = RequestMethod.POST)
	public String wirteCommu(Community community, HttpSession session) {
		
		int pno = community.getpNo();

		community.setmNo(((Integer)session.getAttribute("mNo")).intValue());
		logger.info("넘어온 데이터 확인 {}", community);
		
		communityService.writeCommunity(community);
		
		return "redirect:/community?pNo=" + pno;
	}
	
	
	@RequestMapping(value = "/community/favorite")
	public ModelAndView favorite(Favorite favorite, ModelAndView mav, HttpSession session) {
		
		//찜(하트)
		favorite.setmNo(((Integer)session.getAttribute("mNo")).intValue());
		boolean result = communityService.favorite(favorite);
		
		//좋아요 수 조회
		int cnt = communityService.getTotalCntFavorite(favorite);
		
		mav.addObject("result", result);
		mav.addObject("cnt", cnt);
		mav.setViewName("jsonView");
		
		return mav;
	}
	

}
