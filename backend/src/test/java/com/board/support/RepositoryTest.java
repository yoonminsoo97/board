package com.board.support;

import com.board.global.common.config.JpaAuditConfig;
import com.board.support.config.QuerydslConfig;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import({
        QuerydslConfig.class,
        JpaAuditConfig.class
})
@DataJpaTest
public class RepositoryTest {
}
