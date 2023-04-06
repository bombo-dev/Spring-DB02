package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;

@SpringBootTest
@Slf4j
public class BasicTxTest {

    @Autowired
    PlatformTransactionManager txManager;

    @TestConfiguration
    static class Config {
        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void commit() {
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 커밋 시작");
        txManager.commit(status);
        log.info("트랜잭션 커밋 완료");
    }

    @Test
    void rollback() {
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 롤백 시작");
        txManager.rollback(status);
        log.info("트랜잭션 롤백 완료");
    }

    @Test
    void double_commit() {
        log.info("트랜잭션1 시작");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션1 커밋");
        txManager.commit(tx1);
        log.info("트랜잭션1 커밋 완료");


        log.info("트랜잭션2 시작");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션2 커밋");
        txManager.commit(tx2);
        log.info("트랜잭션2 커밋 완료");
    }

    @Test
    void double_commit_rollback() {
        log.info("트랜잭션1 시작");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션1 커밋");
        txManager.commit(tx1);
        log.info("트랜잭션1 커밋 완료");


        log.info("트랜잭션2 시작");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션2 롤백");
        txManager.rollback(tx2);
        log.info("트랜잭션2 롤백 완료");
    }

    @Test
    void inner_commit() {
        log.info("외부 트랜잭션 시작");
        // DefaultTransactionAttribute 가 DefaultTransactionDefault 를 상속받아 더 많은 기능을 가지고 있음.
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction()={}", outer.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        // 트랜잭션 동기화 매니저를 확인하고, 기존에 진행되던 트랜잭션이 존재한다면 기존 트랜잭션에 참여한다.
        // Participating in existing transaction
        log.info("inner.isNewTransaction()={}", inner.isNewTransaction());
        log.info("내부 트랜잭션 커밋");
        // 이 과정에서 실제로 커밋이 발생하지 않는다. 실제로 커밋이 되버리면 물리 트랜잭션이 커밋되는거나 다름이 없기 때문.
        txManager.commit(inner);

        log.info("외부 트랜잭션 커밋");
        txManager.commit(outer);
    }

    @Test
    void outer_rollback() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction()={}", outer.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("inner.isNewTransaction()={}", inner.isNewTransaction());
        log.info("내부 트랜잭션 커밋");
        // 내부 트랜잭션에서 커밋 시, 물리 트랜잭션에 어떠한 영향도 끼치지 않음.
        txManager.commit(inner);

        log.info("외부 트랜잭션 롤백");
        // 외부 트랜잭션 롤백 시, 해당 트랜잭션에 참여했던 모든 논리 트랜잭션 모두 롤백
        txManager.rollback(outer);
    }

    @Test
    void inner_rollback() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction()={}", outer.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("inner.isNewTransaction()={}", inner.isNewTransaction());
        log.info("내부 트랜잭션 롤백");
        // 물리 트랜잭션에는 어떠한 영향을 끼치지 않음.
        txManager.rollback(inner); // 트랜잭션 동기화 매니저에 rollbackOnly 를 true 로 마킹함.

        log.info("외부 트랜잭션 커밋");

        // 트랜잭션 동기화 매니저에서 rollbackOnly 가 true 인지 확인. true 라면 무조건 rollback 해야하므로, 예상치 못한 동작
        // UnexpectedRollbackException 발생
        Assertions.assertThatThrownBy(() -> txManager.commit(outer))
                .isInstanceOf(UnexpectedRollbackException.class);
    }

    @Test
    void double_rollback() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction()={}", outer.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("inner.isNewTransaction()={}", inner.isNewTransaction());
        log.info("내부 트랜잭션 롤백");
        // 내부에서 동일하게 트랜잭션 동기화 매니저에 rollbackOnly = true 로 변경
        txManager.rollback(inner);

        log.info("외부 트랜잭션 롤백");
        // 외부에서 트랜잭션 동기화 매니저에서 rollbackOnly = true 인 것을 확인하고, 정상 동작
        txManager.rollback(outer);
    }

    @Test
    void inner_rollback_requiresNew() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction()={}", outer.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        // 트랜잭션 옵션을 REQUIRES_NEW 옵션으로 변경
        DefaultTransactionAttribute definition = new DefaultTransactionAttribute();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        // Suspending current transaction(외부 트랜잭션), creating new transaction with name [null]
        TransactionStatus inner = txManager.getTransaction(definition);
        log.info("inner.isNewTransaction()={}", inner.isNewTransaction());

        log.info("내부 트랜잭션 롤백");
        txManager.rollback(inner); // 롤백
        // Resuming suspended transaction(외부 트랜잭션 재 수행) after completion of inner transaction
        log.info("외부 트랜잭션 커밋");
        txManager.commit(outer); // 커밋
    }

    @Test
    void inner_rollback_triple_requires() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction()={}", outer.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("inner.isNewTransaction()={}", inner.isNewTransaction());

        log.info("내부의 내부 트랜잭션 시작");
        TransactionStatus in_inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("inner.isNewTransaction()={}", in_inner.isNewTransaction());
        log.info("내부의 내부 트랜잭션 롤백");
        // 물리 트랜잭션에는 어떠한 영향을 끼치지 않음.
        txManager.rollback(in_inner); // 트랜잭션 동기화 매니저에 rollbackOnly 를 true 로 마킹함.

        log.info("내부 트랜잭션 커밋");
        // 이때는 예외 발생을 따로 하지 않음
        // Participating transaction failed - marking existing transaction as rollback-only 만 표시
        txManager.commit(inner);
        log.info("외부 트랜잭션 커밋");
        // 트랜잭션 동기화 매니저에서 rollbackOnly 가 true 인지 확인. true 라면 무조건 rollback 해야하므로, 예상치 못한 동작
        // UnexpectedRollbackException 발생
        Assertions.assertThatThrownBy(() -> txManager.commit(outer))
                .isInstanceOf(UnexpectedRollbackException.class);
    }
}
