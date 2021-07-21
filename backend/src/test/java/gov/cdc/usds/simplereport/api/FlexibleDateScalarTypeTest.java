package gov.cdc.usds.simplereport.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.AssertionErrors.assertNull;

import graphql.language.StringValue;
import graphql.schema.CoercingParseValueException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.junit.jupiter.api.Test;

public class FlexibleDateScalarTypeTest {
  private final FlexibleDateCoercion converter = new FlexibleDateCoercion();

  @Test
  void convertImpl_succeeds() {
    LocalDate y2k = LocalDate.parse("2000-01-01");
    assertEquals(y2k, converter.convertImpl("2000-01-01"));
    assertEquals(y2k, converter.convertImpl("01/01/2000"));
  }

  @Test
  void convertImpl_slashFormatWorksWithoutLeadingZeros() {
    LocalDate y2k = LocalDate.parse("2000-01-01");
    assertEquals(y2k, converter.convertImpl("1/01/2000"));
    assertEquals(y2k, converter.convertImpl("01/1/2000"));
  }

  @Test
  void convertImpl_twoDigitYear() {
    LocalDate lastCentury = LocalDate.parse("1961-10-12");
    LocalDate thisCentury = LocalDate.parse("2005-05-04");
    assertEquals(lastCentury, converter.convertImpl("10/12/61"));
    assertEquals(thisCentury, converter.convertImpl("5/4/05"));
  }

  @Test
  void convertImpl_returnsNullOnNoSeparators() {
    assertNull(null, converter.convertImpl("20000101"));
  }

  @Test
  void parseLiteral_succeeds() {
    LocalDate y2k = LocalDate.parse("2000-01-01");
    assertEquals(y2k, converter.parseLiteral(new StringValue("2000-01-01")));
    assertEquals(y2k, converter.parseLiteral(new StringValue("01/01/2000")));
  }

  @Test
  void parseValue_succeeds() {
    LocalDate y2k = LocalDate.parse("2000-01-01");
    assertEquals(y2k, converter.parseValue("2000-01-01"));
    assertEquals(y2k, converter.parseValue("01/01/2000"));
    assertEquals(null, converter.parseValue(""));
  }

  @Test
  void parseValue_exceptions() {
    assertThrows(CoercingParseValueException.class, () -> converter.parseValue("20000101"));
    assertThrows(DateTimeParseException.class, () -> converter.parseValue("2000-0101"));
  }
}
