package ppeonfun.controller.admin.board;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import ppeonfun.dto.Board;
import ppeonfun.dto.BoardFile;
import ppeonfun.dto.Comments;
import ppeonfun.dto.Commentss;
import ppeonfun.dto.Recommend;
import ppeonfun.service.admin.board.BoardService;
import ppeonfun.util.Paging;

@Controller("admin.BoardController")
@RequestMapping(value="/admin/board")
public class BoardController {
	private static final Logger logger = LoggerFactory.getLogger(BoardController.class);

	@Autowired
	BoardService boardService;
	
	@RequestMapping(value="/list")
	public String list(
			@RequestParam(value="curPage", defaultValue="0") int cPage
			, @RequestParam(value="category", defaultValue="") String category
			, @RequestParam(value="search", defaultValue="") String search
			, @RequestParam(value="orderby", defaultValue="1") int orderby
			, Model model) {
		logger.info("/admin/board/list [GET] 요청 완료");
		logger.info("얻어온 category : {}", category);
		logger.info("얻어온 search : {}", search);
		
		//카테고리, 검색어 기반 페이징 적용 (없을 시 defaulValue로 전체 글 페이징 적용)
		Paging paging = boardService.getPaging(cPage, category, search);
		
		//model값으로 paging객체 설정
		model.addAttribute("paging", paging);
		
		//추천수 정렬 기준에 따른 리스트 얻어오기 ( 기본적으로 검색기능 적용 )
		// 처음으로 게시판 접속 시 orderby = 1 ( 정렬 미적용 )
		// 정렬 기능 누를 시 내림차순 정렬 orderby = 2 ( 추천수 내림차순 정렬 적용 )
		// 재차 누를 시 오름차순 정렬 ordrby = 3 ( 추천수 오름차순 정렬 적용 )
		if(orderby == 2) {
			List<HashMap<String, Object>> nlist = boardService.getArrayList(paging, category, search, orderby);
			model.addAttribute("nlist", nlist);
			orderby = 3;
			model.addAttribute("orderby", orderby);
			
		} else if(orderby == 3){
			List<HashMap<String, Object>> nlist = boardService.getArrayList(paging, category, search, orderby);
			model.addAttribute("nlist", nlist);
			orderby = 1;
			model.addAttribute("orderby", orderby);
			
		} else {
			//공지사항 리스트 얻어오기
			List<HashMap<String, Object>> nlist = boardService.getList(paging, category, search);
			
			//model값으로 공지사항 리스트 설정
			model.addAttribute("nlist", nlist);
			orderby = 2;
			model.addAttribute("orderby", orderby);
			
		}
			
		return "admin/board/boardList";
	}
	
	
	
	@RequestMapping(value="/write", method=RequestMethod.GET)
	public String writeForm() {
		logger.info("/admin/board/write [GET] 요청 완료");
		
		//viewName writeForm.jsp 지정
		return "admin/board/boardWrite";
	}
	
	
	
	@RequestMapping(value="/write", method=RequestMethod.POST)
	public String write( Board board, MultipartHttpServletRequest mtfRequest, HttpSession session, Model model) {
		logger.info("/admin/board/write [POST] 요청 완료");
		
		//얻어온 다중 파일 리스트에 담기 - 완료
		List<MultipartFile> fileList = mtfRequest.getFiles("file");
		
		int mNo = (Integer)session.getAttribute("mNo");
		board.setmNo(mNo);
		
		logger.info("얻어온 board값 확인 : {}", board);
		logger.info("얻어온 다중 파일 정보 확인 : {}", fileList);
		
		//글 쓰기 수행
		boardService.write(board, fileList);
		
		return "redirect:/admin/board/list";
	}
	
	
	
	@RequestMapping(value="/view")
	public String view(int bNo, HttpSession session, Model model) {
		logger.info("/admin/board/view [GET] 요청 완료");
		logger.info("얻어온 게시글 번호 확인 : {}", bNo);
		
		//해당하는 게시글 번호의 모든 정보 가져오기
		HashMap<String, Object> viewBoard = boardService.getView(bNo);
		
		//얻어온 게시글 정보 확인하기
		logger.info("얻어온 게시글 정보 확인하기 : {}", viewBoard);
		
		//model값으로 게시글 정보 설정
		model.addAttribute("viewBoard", viewBoard);
		
		//첨부파일 리스트 얻어오기
		List<BoardFile> flist = boardService.getFiles(bNo);
		
		logger.info("얻어온 첨부파일 정보 확인하기 : {}", flist);
		
		//model값으로 첨부파일 정보 설정
		model.addAttribute("flist", flist);
		
		//해당 게시글의 추천수 얻어오기
		int rec = boardService.getRecommend(bNo);
		
		//model값으로 추천수 정의
		model.addAttribute("rec", rec);
		
		//로그인 한 유저가 해당 글을 추천했는지 안했는지 확인
		int mNo = (int) session.getAttribute("mNo");
		Recommend recommend = new Recommend();
		recommend.setbNo(bNo);
		recommend.setmNo(mNo);
		boolean chkRec = boardService.chkRecommended(recommend);
		
		//model값으로 추천여부 정의
		model.addAttribute("chkRec", chkRec);
		
		//해당 글의 댓글 리스트 불러오기
		List<HashMap<String, Object>> clist = boardService.getCommentsList(bNo);
		
		logger.info("clist 데이터 확인 : {}", clist);
		
		//model값으로 댓글 정의
		model.addAttribute("clist", clist);
		
		// 해당 글이 가지고있는 댓글들의 댓글 번호 리스트를 얻어서 해당하는 대댓글을 얻어오기
		List<HashMap<String, Object>> cclist = boardService.getCommentssList(bNo);
		
		logger.info("얻어온 대댓글 리스트 : {}", cclist);
		
		//model값으로 대댓글 정의
		model.addAttribute("cclist", cclist);
		
		//viewName 지정
		return "admin/board/boardView";
	}
	
	
	
	@RequestMapping(value="/comments/refresh", method=RequestMethod.GET)
	public String refreshComments(
			int bNo, 
			@RequestParam(value="standard", defaultValue="ASC") String standard, 
			Model model) {
		//댓글과 대댓글 리스트를 얻어온 뒤 model값으로 설정
		List<HashMap<String, Object>> clist = boardService.getCommentsListForArray(bNo, standard);
		model.addAttribute("clist", clist);
		
		List<HashMap<String, Object>> cclist = boardService.getCommentssList(bNo);
		model.addAttribute("cclist", cclist);
		
		model.addAttribute("bNo", bNo);
		model.addAttribute("standard", standard);
		logger.info("모델값으로 설정한 bNo의 값 : {}", bNo);
		logger.info("모델값으로 설정한 standard의 값 : {}", standard);
		
		return "admin/board/boardCmtRefresh";
	}
	
	
	
	@RequestMapping(value="/download")
	public String download( 
			@RequestParam(value="fileno") int bfFileno
			, Model model ) {
		
		//특정 파일번호로 해당 파일의 전체 정보를 얻어온다
		BoardFile bf = boardService.getFile(bfFileno);
		
		//model값으로 download할 파일정보 설정
		model.addAttribute("downFile", bf);
		
		return "down";
	}
	
	
	
	@RequestMapping(value="/recommend")
	public String recommend (Recommend rec, HttpSession session, Model model) {
		int mNo = (int) session.getAttribute("mNo");
		rec.setmNo(mNo);
		
		//recommend 테이블에 값 넣기
		boolean checkRec = boardService.checkRecommend(rec);
		
		logger.info("얻어온 checkRec의 값 : {}", checkRec);
		
		model.addAttribute("chkrec", checkRec);
		
		//해당 게시글의 총 추천수를 얻어와야 한다
		int totalRec = boardService.getRecommend(rec);
		
		logger.info("해당 게시글의 총 조회수 : {}", totalRec);
		
		model.addAttribute("rec", totalRec);
		
		return "admin/board/boardRecResult";
	}
	
	
	
	@RequestMapping(value="/comment/insert")
	public String CmtInsert(Comments cmts, Model model) {
		logger.info("받아온 cmt객체 정보 확인 : {}", cmts);
		
		//새로 입력한 댓글 DB에 삽입
		boardService.writeCmt(cmts);
		
		int bNo = cmts.getbNo();
		
		//새로 입력한 댓글을 포함한 댓글 리스트 조회
		List<HashMap<String, Object>> cmtList = boardService.getCommentsList(bNo);
		
		//model값 전달
		model.addAttribute("cmtList", cmtList);

		List<HashMap<String, Object>> cclist = boardService.getCommentssList(bNo);
		model.addAttribute("cclist", cclist);
		
		return "admin/board/boardCmtInsert";
	}
	
	
	
	@RequestMapping(value="/comment/update", method=RequestMethod.GET)
	public String CmtUpdateForm(Comments cmts, Model model) {
		//해당 댓글 번호의 전체 정보 얻어오기
		HashMap<String, Object> comment = boardService.getCommentForUpdate(cmts);
		
		//model값으로 해당 댓글 정보 설정
		model.addAttribute("cmt", comment);
		
		int bNo = cmts.getbNo();
		
		List<HashMap<String, Object>> cmtList = boardService.getCommentsList(bNo);
		model.addAttribute("cmtList", cmtList);
		
		List<HashMap<String, Object>> cclist = boardService.getCommentssList(bNo);
		model.addAttribute("cclist", cclist);
		
		return "admin/board/boardCmtUpdateForm";
	}
	
	
	
	@RequestMapping(value="/comment/updateCancel", method=RequestMethod.GET)
	public String CmtUpdateCancel(Comments cmts, Model model) {
		logger.info("댓글 수정 취소 시 얻어온 데이터 : {}", cmts);
		
		//댓글 번호로 전체 댓글 데이터 가져오기
		HashMap<String, Object> cmt = boardService.getComments(cmts);
		
		model.addAttribute("c", cmt);
		
		return "admin/board/boardCmtUpdateCancel";
	}
	
	
	
	@RequestMapping(value="/comment/update", method=RequestMethod.POST)
	public String CmtUpdate(Comments cmts, Model model) {
		//입력받은 값을 기존의 댓글 번호에 덮어씌우기
		boardService.updateCmt(cmts);
		
		//덮어씌운 후 해당 글 번호의 전체 댓글 목록 가져오기
		int bNo = cmts.getbNo();
		
		List<HashMap<String, Object>> cmtList = boardService.getCommentsList(bNo);
		model.addAttribute("cmtList", cmtList);

		List<HashMap<String, Object>> cclist = boardService.getCommentssList(bNo);
		model.addAttribute("cclist", cclist);

		return "admin/board/boardCmtUpdate";
	}
	
	
	
	@RequestMapping(value="/comment/delete")
	public String CmtDelete(Comments cmts, Model model) {
		//해당 댓글의 대댓글 소유 여부에 따라 삭제할지, 따로 관리할지 결정한다
		boardService.deleteCmt(cmts);
		
		int bNo = cmts.getbNo();
		
		List<HashMap<String, Object>> cmtList = boardService.getCommentsList(bNo);
		model.addAttribute("cmtList", cmtList);
		
		List<HashMap<String, Object>> cclist = boardService.getCommentssList(bNo);
		model.addAttribute("cclist", cclist);

		return "admin/board/boardCmtDelete";
	}
	
	
	
	@RequestMapping(value="/comments/insert")
	public String CmtCmtInsert(Commentss cmtss, int bNo, Model model) {
		logger.info("답글 쓰기 후 등록 시 얻어오는 데이터 : {}", cmtss);
		
		//얻어온 신규 대댓글 데이터 DB 삽입
		boardService.writeCmtCmt(cmtss);
		
		List<HashMap<String, Object>> clist = boardService.getCommentsList(bNo);
		model.addAttribute("clist", clist);
		
		//삽입한 신규 대댓글을 포함한 전체 대댓글 리스트 얻어오기
		List<HashMap<String, Object>> cclist = boardService.getCommentssList(bNo);
		model.addAttribute("cclist", cclist);
		
		return "admin/board/boardCmtCmtInsert";
	}
	
	
	
	@RequestMapping(value="/commentss/insert", method=RequestMethod.GET)
	public String CmtssInsertFormAfterCmtss(Commentss cmtss, String mNick, Model model) {
		logger.info("얻어온 cmtss 데이터 : {}", cmtss);
		Commentss result = boardService.getOneCommentss(cmtss);

		model.addAttribute("cmtss", result);
		model.addAttribute("mNick", mNick);
		
		return "admin/board/boardCmtssInsertForm";
	}
	
	
	
	@RequestMapping(value="commentss/insert", method=RequestMethod.POST)
	public String CmtssInsertAfterCmtss(Commentss cmtss, int bNo , Model model) {
		boardService.writeCmtCmt(cmtss);
		
		List<HashMap<String, Object>> clist = boardService.getCommentsList(bNo);
		model.addAttribute("clist", clist);
		
		//삽입한 신규 대댓글을 포함한 전체 대댓글 리스트 얻어오기
		List<HashMap<String, Object>> cclist = boardService.getCommentssList(bNo);
		model.addAttribute("cclist", cclist);
		
		return "admin/board/boardCmtCmtInsert";
	}
	
	
	@RequestMapping(value="/commentss/insertcancel", method=RequestMethod.GET)
	public String CmtCmtInsertCancel(Commentss cmtss, String mNick, int bNo, Model model) {
		Commentss result = boardService.getOneCommentss(cmtss);
		
		model.addAttribute("cmtss", result);
		model.addAttribute("mNick", mNick);
		model.addAttribute("bNo", bNo);
		
		return "admin/board/boardCmtCmtInsertCancel";
	}
	
	
	
	@RequestMapping(value="/commentss/update", method=RequestMethod.GET)
	public String CmtCmtUpdateForm(Commentss cmtss, String mNick, Model model) {
		logger.info("commentss update용으로 얻어온 기존 대댓글 데이터 : {}", cmtss);
		logger.info("수정하려는 대댓글의 회원 닉네임 : {}", mNick);
		model.addAttribute("cmtss", cmtss);
		model.addAttribute("mNick", mNick);
		
		return "admin/board/boardCmtCmtUpdateForm";
	}
	
	
	@RequestMapping(value="/commentss/update", method=RequestMethod.POST)
	public String CmtssUpdateAfterCmts(Commentss cmtss, String mNick, int bNo, Model model) {
		logger.info("대댓글 수정 폼에서 입력한 값 확인 : {}", cmtss);
		
		//변경한 내용으로 대댓글 데이터 Update 수행
		boardService.updateCmtCmt(cmtss);
		
		Commentss cmtss1 = boardService.getOneCommentss(cmtss);
		model.addAttribute("cmtss", cmtss1);
		model.addAttribute("mNick", mNick);
		model.addAttribute("bNo", bNo);
		
		return "admin/board/boardCmtCmtUpdate";
	}
	
	@RequestMapping(value="/commentss/updatecancel", method=RequestMethod.GET)
	public String CmtssUpdateCancelAfterCmts(Commentss cmtss, String mNick, int bNo, Model model) {
		logger.info("얻어온 cmtss 데이터 확인 : {}", cmtss);
		
		//해당 댓글 다시 가져오기
		Commentss result = boardService.getOneCommentss(cmtss);
		model.addAttribute("cmtss", result);
		model.addAttribute("mNick", mNick);
		model.addAttribute("bNo", bNo);
		
		return "admin/board/boardCmtCmtUpdateCancel";
	}
	
	
	@RequestMapping(value="/commentss/delete")
	public String CmtCmtDelete(int csNo, int bNo, Model model) {
		logger.info("얻어온 csNo 값 확인 : {}", csNo);
		logger.info("얻어온 bNo 값 확인 : {}", bNo);
		
		//해당 대댓글 삭제
		boardService.deleteCmtCmt(csNo);
		
		List<HashMap<String, Object>> clist = boardService.getCommentsList(bNo);
		model.addAttribute("clist", clist);
		
		//삽입한 신규 대댓글을 포함한 전체 대댓글 리스트 얻어오기
		List<HashMap<String, Object>> cclist = boardService.getCommentssList(bNo);
		model.addAttribute("cclist", cclist);
		
		return "admin/board/boardCmtCmtDelete";
	}
	
	
	
	@RequestMapping(value="/update", method=RequestMethod.GET)
	public String updateForm( int bNo, Model model ) {
		logger.info("얻어온 bNo값 확인 : {}", bNo);
		
		//얻어온 값으로 전체 공지사항 정보 얻어오기
		Board board = boardService.getViewForUpdate(bNo);
		
		logger.info("얻어온 board 전체 정보 확인 : {}", board);
		
		//model값으로 공지사항 객체 설정
		model.addAttribute("board", board);
		
		//해당 게시글의 첨부파일 불러오기
		List<BoardFile> flist = boardService.getFiles(bNo);
		
		//model값으로 첨부파일 리스트 설정
		model.addAttribute("flist", flist);
		
		//viewName 설정
		return "admin/board/boardUpdate";
	}
	
	
	
	@RequestMapping(value="/update", method=RequestMethod.POST)
	public String update( Board board, MultipartHttpServletRequest mtfRequest) {
		logger.info("얻어온 board객체 정보 확인 {}", board);
		
		//다중 첨부파일 리스트로 변환
		List<MultipartFile> flist = mtfRequest.getFiles("file");
		
		for( MultipartFile i : flist ) {
			logger.info("각각의 다중 첨부파일 정보 확인 {}", i);
		}
		
		//글 수정 메소드 호출
		boardService.updateBoardAndFiles(board, flist);
		
		return "redirect:/admin/board/list";
	}
	
	
	
	@RequestMapping(value="/delete")
	public String delete(Board board) {
		logger.info("/admin/board/delete 요청 완료");
		
		//해당 글 번호의 공지사항 삭제
		boardService.deleteBoard(board);
		
		return "redirect:/admin/board/list";
	}

}
