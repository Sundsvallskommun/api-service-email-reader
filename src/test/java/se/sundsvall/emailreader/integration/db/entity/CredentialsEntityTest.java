package se.sundsvall.emailreader.integration.db.entity;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.LocalDateTime;
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
            .withNamespace("someNamespace")
            .withDestinationFolder("someDestinationFolder")
            .withCreatedAt(LocalDateTime.now())
            .build();

        Assertions.assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
    }

}