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

class AttachmentEntityTest {

    @Test
    void testBean() {

        BeanMatchers.registerValueGenerator(LocalDateTime::now, LocalDateTime.class);

        MatcherAssert.assertThat(AttachmentEntity.class, allOf(
            hasValidBeanConstructor(),
            hasValidGettersAndSetters(),
            hasValidBeanHashCode(),
            hasValidBeanEquals(),
            hasValidBeanToString()));
    }

    @Test
    void testFields() {

        final var object = AttachmentEntity.builder()
            .withId(new Random().nextLong())
            .withName("someName")
            .withContent("someContent")
            .withContentType("someContentType")
            .withCreatedAt(LocalDateTime.now())
            .build();

        Assertions.assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
    }

}