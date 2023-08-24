package se.sundsvall.emailreader.integration.db;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import com.google.code.beanmatchers.BeanMatchers;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class EmailEntityTest {

    @Test
    void testBean() {

        BeanMatchers.registerValueGenerator(LocalDateTime::now, LocalDateTime.class);

        MatcherAssert.assertThat(EmailEntity.class, allOf(
            hasValidBeanConstructor(),
            hasValidGettersAndSetters(),
            hasValidBeanHashCode(),
            hasValidBeanEquals(),
            hasValidBeanToString()));
    }

    @Test
    void testFields() {

        final var object = EmailEntity.builder()
            .withId(String.valueOf(new Random().nextInt()))
            .withNamespace("someNamespace")
            .withMunicipalityId("someMunicipalityId")
            .withTo(List.of("someTo"))
            .withFrom("someFrom")
            .withSubject("someSubject")
            .withMessage("someMessage")
            .withCreatedAt(LocalDateTime.now())
            .withAttachments(List.of(AttachmentEntity.builder()
                .withName("someName")
                .withContent("someContent")
                .withContentType("someContentType")
                .build())).build();

        Assertions.assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
    }

}
