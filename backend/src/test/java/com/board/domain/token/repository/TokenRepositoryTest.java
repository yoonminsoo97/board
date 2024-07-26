package com.board.domain.token.repository;

import com.board.domain.member.entity.Member;
import com.board.domain.member.repository.MemberRepository;
import com.board.domain.token.entity.Token;
import com.board.support.RepositoryTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenRepositoryTest extends RepositoryTest {

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member memberA;
    private Member memberB;

    @BeforeEach
    void setUp() {
        memberA = Member.builder()
                .nickname("yoonkunA")
                .username("yoon1234")
                .password(new BCryptPasswordEncoder().encode("12345678"))
                .build();
        memberB = Member.builder()
                .nickname("yoonkunB")
                .username("yoon5678")
                .password(new BCryptPasswordEncoder().encode("12345678"))
                .build();
        memberRepository.save(memberA);
        memberRepository.save(memberB);
    }

    @Nested
    @DisplayName("토큰 저장")
    class TokenSaveTest {

        @Test
        @DisplayName("토큰을 저장한다")
        void save() {
            Token token = Token.builder()
                    .refreshToken("refresh-token")
                    .member(memberA)
                    .build();

            Token saveToken = tokenRepository.save(token);

            assertThat(saveToken.getId()).isNotNull();
        }

        @Test
        @DisplayName("토큰 저장 시 리프레시 토큰 값이 동일하면 예외가 발생한다")
        void saveUniqueRefreshToken() {
            Token tokenA = Token.builder()
                    .refreshToken("refresh-token")
                    .member(memberA)
                    .build();
            Token tokenB = Token.builder()
                    .refreshToken("refresh-token")
                    .member(memberB)
                    .build();
            tokenRepository.save(tokenA);

            assertThatThrownBy(() -> tokenRepository.save(tokenB))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("토큰 저장시 회원 값이 동일하면 예외가 발생한다")
        void saveUniqueMemberToken() {
            Token tokenA = Token.builder()
                    .refreshToken("refresh-tokenA")
                    .member(memberA)
                    .build();
            Token tokenB = Token.builder()
                    .refreshToken("refresh-tokenB")
                    .member(memberA)
                    .build();
            tokenRepository.save(tokenA);

            assertThatThrownBy(() -> tokenRepository.save(tokenB))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("토큰 저장 시 리프레시 토큰 값이 Null이면 예외가 발생한다")
        void saveNullRefeshToken() {
            Token tokenA = Token.builder()
                    .refreshToken(null)
                    .member(memberA)
                    .build();

            assertThatThrownBy(() -> tokenRepository.save(tokenA))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("토큰 저장 시 회원 값이 Null이면 예외가 발생한다")
        void saveNullMember() {
            Token tokenA = Token.builder()
                    .refreshToken("refresh-token")
                    .member(null)
                    .build();

            assertThatThrownBy(() -> tokenRepository.save(tokenA))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

    }

    @Nested
    @DisplayName("토큰 조회")
    class TokenFindTest {

        @Test
        @DisplayName("회원 아이디(id)로 토큰을 조회한다")
        void findTokenByMemberId() {
            Token token = Token.builder()
                    .refreshToken("refresh-token")
                    .member(memberA)
                    .build();
            Token saveToken = tokenRepository.save(token);

            Token findtoken = tokenRepository.findByMemberId(memberA.getId())
                    .orElseThrow();

            assertThat(findtoken.getId()).isEqualTo(saveToken.getId());
            assertThat(findtoken.getRefreshToken()).isEqualTo("refresh-token");
        }

        @Test
        @DisplayName("리프레시 토큰으로 토큰과 회원을 한번에 조회한다")
        void findTokenByMemberIdJoinFetchMember() {
            Token token = Token.builder()
                    .refreshToken("refresh-token")
                    .member(memberA)
                    .build();
            Token saveToken = tokenRepository.save(token);

            Token findToken = tokenRepository.findByRefreshTokenJoinFetchMember("refresh-token")
                    .orElseThrow();

            assertThat(findToken.getId()).isEqualTo(saveToken.getId());
            assertThat(findToken.getRefreshToken()).isEqualTo("refresh-token");
            assertThat(findToken.getMember().getId()).isEqualTo(memberA.getId());
            assertThat(findToken.getMember().getNickname()).isEqualTo("yoonkunA");
            assertThat(findToken.getMember().getUsername()).isEqualTo("yoon1234");
            assertThat(findToken.getMember().getAuthority()).isEqualTo("ROLE_MEMBER");
        }

    }

    @Nested
    @DisplayName("토큰 삭제")
    class TokenDeleteTest {

        @Test
        @DisplayName("토큰을 삭제한다")
        void deleteToken() {
            Token token = Token.builder()
                    .refreshToken("refresh-token")
                    .member(memberA)
                    .build();
            tokenRepository.save(token);

            Token findToken = tokenRepository.findByMemberId(memberA.getId()).orElseThrow();
            tokenRepository.delete(findToken);

            assertThat(tokenRepository.findByMemberId(memberA.getId())).isEmpty();
        }

    }

}