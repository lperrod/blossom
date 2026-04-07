package com.blossomproject.core.common.utils.mail;

import java.util.concurrent.CompletableFuture;

/**
 * An email ready to be sent
 *
 * @author rlejolivet
 */
public interface BlossomMail {

  /**
   * Send the email as is, synchronously.
   */
  void send() throws Exception;

  /**
   * Send the email asynchronously, using an AsyncMailSender
   *
   * @return
   */
  CompletableFuture<BlossomMail> asyncSend();

}
