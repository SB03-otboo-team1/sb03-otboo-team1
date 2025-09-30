package com.onepiece.otboo.domain.dm.exception;

public class CannotSendMessageToSelfException extends RuntimeException {
  public CannotSendMessageToSelfException(String message) {
    super(message);
  }
}
