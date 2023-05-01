package web.instaweb.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import web.instaweb.domain.Member;
import web.instaweb.domain.Page;

import javax.persistence.EntityManager;
import java.util.*;

@Repository
@RequiredArgsConstructor
@Transactional
public class MemberRepository {

    private final EntityManager em;

    public void save(Member member) {
        em.persist(member);
    }

    public Member findById(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    public void delete(Long id) {
        Member member = em.find(Member.class, id);
        em.remove(member);
    }

    // loginId 로 Member 찾는다
    // 해당 없다면 null 리턴
    public Optional<Member> findByLoginId(String loginId) {

        return em.createQuery("select m from Member m where m.loginId = :loginId", Member.class)
                .setParameter("loginId", loginId)
                .getResultList()
                .stream()
                .filter(m -> m.getLoginId().equals(loginId)).findFirst();
    }

    // name 로 Member 찾는다
    public Optional<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList()
                .stream()
                .filter(m -> m.getName().equals(name)).findFirst();
    }

}

