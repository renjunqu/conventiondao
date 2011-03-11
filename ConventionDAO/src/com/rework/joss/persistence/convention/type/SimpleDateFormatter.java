package com.rework.joss.persistence.convention.type;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleDateFormatter {
  public static Date format(String format, String datetime) {
    try {
      return new SimpleDateFormat(format).parse(datetime);
    } catch (ParseException e) {
      throw new SqlMapException("Error parsing default null value date.  Format must be '" + format + "'. Cause: " + e);
    }
  }

}
