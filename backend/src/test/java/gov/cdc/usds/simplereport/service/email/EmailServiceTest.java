package gov.cdc.usds.simplereport.service.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sendgrid.helpers.mail.Mail;
import gov.cdc.usds.simplereport.api.model.TemplateVariablesProvider;
import gov.cdc.usds.simplereport.properties.SendGridProperties;
import gov.cdc.usds.simplereport.service.BaseServiceTest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.thymeleaf.spring5.SpringTemplateEngine;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest extends BaseServiceTest<EmailService> {
  @Qualifier("simpleReportTemplateEngine")
  @Autowired
  SpringTemplateEngine _templateEngine;

  private static final SendGridProperties FAKE_PROPERTIES =
      new SendGridProperties(true, null, "me@example.com", List.of(), List.of());

  @Mock EmailProvider mockSendGrid;
  @Captor ArgumentCaptor<Mail> mail;

  private EmailService _service;

  @BeforeEach
  void initService() {
    _service = new EmailService(mockSendGrid, FAKE_PROPERTIES, _templateEngine);
  }

  public static class FooBarTemplate implements TemplateVariablesProvider {
    private final String foo;
    private final String bar;

    public FooBarTemplate(final String foo, final String bar) {
      this.foo = foo;
      this.bar = bar;
    }

    @Override
    public String getTemplateName() {
      return "test-template";
    }

    @Override
    public Map<String, Object> toTemplateVariables() {
      return new HashMap<>() {
        {
          put("foo", foo);
          put("bar", bar);
        }
      };
    }
  }

  @Test
  void sendEmail() throws IOException {

    // GIVEN
    String toEmail = "test@foo.com";
    String subject = "Testing the email service";
    FooBarTemplate fbTemplate = new FooBarTemplate("var 1", "var 2");

    // WHEN
    _service.send(toEmail, subject, fbTemplate);

    // THEN
    verify(mockSendGrid, times(1)).send(mail.capture());
    assertEquals(mail.getValue().getPersonalization().get(0).getTos().get(0).getEmail(), toEmail);
    assertEquals(mail.getValue().getSubject(), subject);
    assertThat(mail.getValue().getContent().get(0).getValue())
        .contains("<b>Foo:</b> var 1", "<b>Bar:</b> var 2");
  }

  @Test
  void sendMultiRecipientEmail() throws IOException {
    // GIVEN
    List<String> tos =
        List.of(
            "test@foo.com",
            "another@foo.com",
            "apple@foo.com",
            "banana@foo.com",
            "onemore@foo.com");
    String subject = "Testing the email service";
    FooBarTemplate fbTemplate = new FooBarTemplate("var 1", "var 2");

    // WHEN
    _service.send(tos, subject, fbTemplate);

    // THEN
    verify(mockSendGrid, times(1)).send(mail.capture());
    for (int i = 0; i < tos.size(); i++) {
      assertEquals(
          mail.getValue().getPersonalization().get(0).getTos().get(i).getEmail(), tos.get(i));
    }
    assertEquals(mail.getValue().getSubject(), subject);
    assertThat(mail.getValue().getContent().get(0).getValue())
        .contains("<b>Foo:</b> var 1", "<b>Bar:</b> var 2");
  }
}
