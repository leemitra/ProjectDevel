package com.ruby.devel.web;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.ruby.devel.model.MarketDto;
import com.ruby.devel.service.MarketMapper;


@Controller
public class MarketplaceController {
	
	@Autowired //자동주입
	MarketMapper Mmapper;
	
	//마켓 기본 페이지
	@GetMapping({"/marketplace","/marketplace/market_main"})
	public ModelAndView marketplace_home(
			@RequestParam (value = "currentPage",defaultValue = "1") int currentPage)
	{
		ModelAndView mview = new ModelAndView();
		
		int totalCount = Mmapper.getTotalCount();
		//List<MarketDto> list = mapper.getAllDatas();
		
		//페이징처리에 필요한 변수
		int totalPage; //총 페이지수
		int startPage; //각블럭의 시작페이지
		int endPage; //각블럭의 끝페이지
		int start; //각페이지의 시작번호..한페이지에서 보여질 시작 글 번호(인덱스에서 보여지는 번호)
		int perPage=8; //한페이지에 보여질 글 갯수
		int perBlock=2; //한블럭당 보여지는 페이지 개수
		
		//총페이지 개수구하기
		totalPage=totalCount/perPage+(totalCount%perPage==0?0:1);
							
		//각블럭의 시작페이지
		startPage=(currentPage-1)/perBlock*perBlock+1;
		endPage=startPage+perBlock-1;
							
		if(endPage>totalPage)
			endPage=totalPage;
				
		//각페이지에서 불러올 시작번호
		start=(currentPage-1)*perPage;
						
		//service 안 넣을 경우
		//데이타 가져오기..map처리
		HashMap<String, Integer> map = new HashMap<>();
		map.put("start", start);
		map.put("perPage", perPage);
						
		//각페이지에서 필요한 게시글 가져오기
		List<MarketDto> list=Mmapper.getList(map);
								
		//각 글앞에 붙일 시작번호 구하기
		//총글이 20개면? 1페이지 20 2페이지 15부터 출력해서 1씩 감소
		int no=totalCount-(currentPage-1)*perPage;
								
		//출력에 필요한 변수들을 request 에 저장
		mview.addObject("list",list);
		mview.addObject("startPage",startPage);
		mview.addObject("endPage",endPage);
		mview.addObject("totalPage",totalPage);
		mview.addObject("totalCount",totalCount);
		mview.addObject("no",no);
		mview.addObject("currentPage",currentPage);
		mview.addObject("totalCount",totalCount);

		mview.addObject("list", list);

		mview.setViewName("m/marketplace/market_main");
				
		return mview;
	}

	
	//상품등록 페이지 mapping
	@GetMapping("/marketplace/productadd")
	public String marketplace_productadd() {
		return "/marketplace/market_productAddForm";
	}
	
	//상품등록 insert
	@PostMapping("/marketplace/insert")
	public String insert(
			HttpSession session,
			@RequestParam ArrayList<MultipartFile> photos,
			@ModelAttribute MarketDto dto)
	{
		//이미지 저장 경로
		String path = session.getServletContext().getRealPath("/photo"); //webapp/photo 에 바로 넣기
		System.out.println(path);
		
		String photo="";
		if(photos.get(0).getOriginalFilename().equals("")) //첫번째가 빈 문자열이면
			photo="no";
		else {
			for(MultipartFile f:photos)
			{
				String fName = f.getOriginalFilename();
				photo += fName+","; //photo에 fName을 누적
				
				try {
					f.transferTo(new File(path+"\\"+fName));
				} catch (IllegalStateException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			photo = photo.substring(0, photo.length()-1);
		}
		
		dto.setPhoto(photo);
		
		//insert
		Mmapper.insertMarket(dto);
				
		//완료 후 목록 이동
		return "redirect:market_main";
	}

	
	@GetMapping("/marketplace/productdetail")		// 상품 상세 페이지
	public String marketplace_productdetail() {
		return "/marketplace/market_productDetail";
	}
	
}