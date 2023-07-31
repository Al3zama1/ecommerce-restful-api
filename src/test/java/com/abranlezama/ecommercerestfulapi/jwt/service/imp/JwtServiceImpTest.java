package com.abranlezama.ecommercerestfulapi.jwt.service.imp;

import com.abranlezama.ecommercerestfulapi.objectMother.UserObjectMother;
import com.abranlezama.ecommercerestfulapi.user.model.User;
import com.abranlezama.ecommercerestfulapi.user.service.imp.SecurityService;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("jwt service")
class JwtServiceImpTest {

    @Mock
    private Clock clock;
    @InjectMocks
    private JwtServiceImp cut;


    @Test
    void shouldCreateJWTFromUserDetails() throws NoSuchFieldException, IllegalAccessException {
        // Given
        Field field = JwtServiceImp.class.getDeclaredField("jwtSecret");
        field.setAccessible(true);
        field.set(cut, "slfjsldjflsjdfljsldfjsldfjdf");

        LocalDateTime defaultLocalDateTime = LocalDateTime.of(2023, 7, 24, 11, 23);
        Clock fixedClock = Clock.fixed(defaultLocalDateTime.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
        User user = UserObjectMother.customer().build();
        UserDetails userDetails = new SecurityService.UserPrincipal(user);

        given(clock.instant()).willReturn(fixedClock.instant());

        // When
        String accessToken = cut.createAccessToken(userDetails);


        // Then
        assertThat(cut.getSubject(accessToken)).isEqualTo(user.getEmail());
    }

    @Test
    void shouldNotGetTokenSubjectWhenItHasExpired() throws NoSuchFieldException, IllegalAccessException {
        // Given
        Field field = JwtServiceImp.class.getDeclaredField("jwtSecret");
        field.setAccessible(true);
        field.set(cut, "slfjsldjflsjdfljsldfjsldfjdf");

        LocalDateTime defaultLocalDateTime = LocalDateTime.of(2022, 7, 24, 11, 23);
        Clock fixedClock = Clock.fixed(defaultLocalDateTime.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
        User user = UserObjectMother.customer().build();
        UserDetails userDetails = new SecurityService.UserPrincipal(user);

        given(clock.instant()).willReturn(fixedClock.instant());

        // When
        String accessToken = cut.createAccessToken(userDetails);
        assertThatThrownBy(() -> cut.getSubject(accessToken))
                .isInstanceOf(TokenExpiredException.class);

        // Then
    }

}
