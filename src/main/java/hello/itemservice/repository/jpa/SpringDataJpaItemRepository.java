package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataJpaItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByItemNameLike(String itemName);
    List<Item> findByPriceLessThanEqual(Integer price);

    // 쿼리 메서드 (아래 메서드와 같은 기능 수행)
    List<Item> findByItemNameLikeAndPriceLessThanEquals(String itemName, Integer price);

    // 서비스 로직에서는 단일 책임 원칙을 지켜서 함수명을 줄이는 것이 좋지만, 쿼리 같은 경우는 조건자때문에 함수명이 길어지는 문제가 있다.
    // 때문에 길게 늘리지 말고 추상화된 메서드 명으로 주석을 달아놓는 것이 더 좋다.
    // 쿼리 직접 실행
    @Query("select i from Item i where i.itemName like :itemName and i.price <= :price")
    List<Item> findItems(@Param("itemName") String itemName, @Param("price") Integer price);
}
