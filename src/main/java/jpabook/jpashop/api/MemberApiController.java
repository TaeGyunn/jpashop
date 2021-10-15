package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;
    
    // @get -> @post -> @put -> @delete순으로 나열하면 깔끔하다

    @GetMapping("/api/v1/members")
    public List<Member> membersV1(){
        /*  문제점
            응답 값으로 엔티티를 직접 외부에 노출
            엔티티에 프레젠테이션 계층을 위한 로직이 추가된다
            응답 스펙을 맞추기 위해 로직이 추가
            엔티기가 변경되면 API스펙이 변하게 된다.
         */
        return memberService.findMembers();
    }

    //멤버 조회 (json으로 보낼때 꼭 Entity 그대로 보내지 말고 DTO로 필요한 것만 보내기)
    //List로 감싸서 보내주는것이 좋다.
    @GetMapping("/api/v2/members")
    public Result memberV2(){

        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect);
    }

    @Data
    @AllArgsConstructor
    public class Result<T>{
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto{
        private String name;
    }

    //Entity 그대로 사용
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member){
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    //DTO 사용
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request){

        Member member = new Member();
        member.setName(request.name);

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }
    
    // member 수정
    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id,
                                               @RequestBody @Valid UpdateMemberRequest request){

        memberService.update(id, request.getName());
        // 위에서 member나 member.id로 반환하지 않는 이유는 커맨드와 쿼리가 같이 있는 상태가 되기 때문이다.
        Member findMember = memberService.findOne(id);
        // id값은 필요할 수 있기 때문에 따로 출력해준다. 이처럼 커맨드와 쿼리를 분리해주는것이 유지보수에 좋다
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @Data
    static class UpdateMemberRequest{
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse{
        private Long ud;
        private String name;
    }

    @Data
    static class CreateMemberRequest{
        private String name;
    }

    @Data
    private class CreateMemberResponse {

        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }
}
