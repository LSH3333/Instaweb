package web.instaweb.service;

import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.instaweb.domain.Member;
import web.instaweb.domain.Page;
import web.instaweb.repository.MemberRepository;

import javax.persistence.EntityManager;
import java.util.Optional;

@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public void saveMember(Member member) {
        memberRepository.save(member);
    }

    public Member findOne(Long id) {
        return memberRepository.findById(id);
    }

    // member 의 loginId 가 이미 db 에 존재하면 return false
    public boolean checkLoginIdDuplication(Member member) {
        return memberRepository.findByLoginId(member.getLoginId()).isPresent();
    }

    public boolean checkNameDuplication(Member member) {
        return memberRepository.findByName(member.getName()).isPresent();
    }

    public Member registerNewMember(String loginId, String password, String name) {
        Member member = new Member();
        member.setLoginId(loginId);
        member.setPassword(password);
        member.setName(name);
        memberRepository.save(member);
        return member;
    }

    public void setMemberWritingPageId(Long memberId, Long writingPageId) {
        Member member = memberRepository.findById(memberId);
        member.setWritingPageId(writingPageId);
    }
}
