package se.sundsvall.emailreader.integration.db.entity;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Random;

import com.google.code.beanmatchers.BeanMatchers;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class CredentialsEntityTest {

    @Test
    void testBean() {

        BeanMatchers.registerValueGenerator(LocalDateTime::now, LocalDateTime.class);

        MatcherAssert.assertThat(CredentialsEntity.class, allOf(
            hasValidBeanConstructor(),
            hasValidGettersAndSetters(),
            hasValidBeanHashCode(),
            hasValidBeanEquals(),
            hasValidBeanToString()));
    }

    @Test
    void testFields() {

        final var object = CredentialsEntity.builder()
            .withId(String.valueOf(new Random().nextInt()))
            .withUsername("someUsername")
            .withPassword("somePassword")
            .withDomain("someDomain")
            .withMunicipalityId("someMunicipalityId")
            .withEmailAdress(Collections.singletonList("someEmailAdress"))
            .withNamespace("someNamespace")
            .withDestinationFolder("someDestinationFolder")
            .withCreatedAt(LocalDateTime.now())
            .build();

        object.prePersist();
        assertThat(object.getCreatedAt()).isCloseTo(now(), within(1, SECONDS));
        Assertions.assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
    }

}
