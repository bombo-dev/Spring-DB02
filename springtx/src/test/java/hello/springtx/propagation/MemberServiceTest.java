package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

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

    /**
     * memberService @Transactional : ON
     * memberRepository @Transactional : ON
     * logRepository @Transactional : ON Exception
     */
    // 각 트랜잭션들이 참여를 하고, 하나의 논리 트랜잭션에서 에외가 발생해 처리를 하지 못하면 rollbackOnly = true 로 변환.
    @Test
    void outerTxOn_fail() {
        // given
        String username = "로그예외_outerTxOn_fail";

        // when
        Assertions.assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);

        // then
        // 예외 발생 시 물리 트랜잭션은 롤백된다.
        Assertions.assertThat(memberRepository.find(username).isPresent()).isFalse();
        Assertions.assertThat(logRepository.find(username).isPresent()).isFalse();
    }

    /**
     * memberService @Transactional : ON
     * memberRepository @Transactional : ON
     * logRepository @Transactional : ON Exception
     */
    // 각 트랜잭션들이 참여를 하고, 하나의 논리 트랜잭션에서 에외가 발생해 처리를 하지 못하면 rollbackOnly = true 로 변환.
    @Test
    void recoverException_fail() {
        // given
        String username = "로그예외_recoverException_fail";

        // when
        Assertions.assertThatThrownBy(() -> memberService.joinV2(username))
                .isInstanceOf(UnexpectedRollbackException.class);

        // then
        // 예외를 잡아서 회원 저장에 대한 서비스 로직은 True 로 예상하나, rollbackOnly = true 옵션으로 인하여, UnExpectedRollback 예외가 발생한다.
        Assertions.assertThat(memberRepository.find(username).isPresent()).isFalse();
        Assertions.assertThat(logRepository.find(username).isPresent()).isFalse();
    }
}