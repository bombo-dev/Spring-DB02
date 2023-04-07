package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    LogRepository logRepository;


    /**
     * memberService @Transactional:OFF
     * memberRepository @Transactional:ON
     * logRepository @Transactional : ON
     */
    @Test
    void outerTxOff_success() {
        //given
        String username = "outerTxOff_success";

        //when
        memberService.joinV1(username);

        //then
        Assertions.assertThat(memberRepository.find(username).isPresent()).isTrue();
        Assertions.assertThat(logRepository.find(username).isPresent()).isTrue();
    }

    /**
     * memberService @Transactional:OFF
     * memberRepository @Transactional:ON
     * logRepository @Transactional : ON Exception
     */
    @Test
    void outerTxOff_fail() {
        //given
        String username = "로그예외_outerTxOff_fail";

        //when
        Assertions.assertThatThrownBy(() -> memberService.joinV1(username))
                        .isInstanceOf(RuntimeException.class);

        //then
        // 예외 발생 시 정상 흐름이 되도록 처리해두었음.
        Assertions.assertThat(memberRepository.find(username).isPresent()).isTrue();
        Assertions.assertThat(logRepository.find(username).isPresent()).isFalse();
    }

    /**
     * memberService @Transactional:ON
     * memberRepository @Transactional:OFF
     * logRepository @Transactional : OFF
     */
    // 트랜잭션을 처음에만 만들고, 이후에 생성하지 않음.
    @Test
    void singleTx() {
        //given
        String username = "outerTxOff_success";

        //when
        memberService.joinV1(username);

        //then
        Assertions.assertThat(memberRepository.find(username).isPresent()).isTrue();
        Assertions.assertThat(logRepository.find(username).isPresent()).isTrue();
    }

    /**
     * memberService @Transactional: ON
     * memberRepository @Transactional: ON
     * logRepository @Transactional : ON
     */
    // 각 저장소 쿼리의 트랜잭션들은 기존에 존재하던 트랜잭션에 참가를 한다.
    @Test
    void outerTxOn_success() {
        //given
        String username = "outerTxOn_success";

        //when
        memberService.joinV1(username);

        //then
        Assertions.assertThat(memberRepository.find(username).isPresent()).isTrue();
        Assertions.assertThat(logRepository.find(username).isPresent()).isTrue();
    }
}