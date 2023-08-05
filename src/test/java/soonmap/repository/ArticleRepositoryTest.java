package soonmap.repository;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import soonmap.entity.AccountType;
import soonmap.entity.Article;
import soonmap.entity.ArticleType;
import soonmap.entity.Member;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ArticleRepositoryTest {

    @Autowired
    ArticleRepository articleRepository;

    @Test
    @Disabled
    void articleuploadtest() {
        // given
        Article save = articleRepository.save(Article.builder()
                .title("testTitle")
                .content("testContent")
                .articleType(new ArticleType(1L, "testType", "tempt"))
                .createAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .member(new Member(1L, "testid1", "test1@email.com", "test1", "testPassword1", AccountType.ADMIN, false, true, true, true, "testSnsId", LocalDateTime.now()))
                .view(0)
                .build());

        // when
        List<Article> all = articleRepository.findAll();

        // then
        assertEquals(all.size(),1);
    }
}