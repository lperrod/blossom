package com.blossomproject.core.common.utils.mail;

import com.google.common.base.Preconditions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.transactional.Attachment;
import com.mailjet.client.transactional.SendContact;
import com.mailjet.client.transactional.SendEmailsRequest;
import com.mailjet.client.transactional.TransactionalEmail;
import freemarker.template.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.concurrent.ListenableFuture;

import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MailSenderImpl extends DeprecatedMailSenderImpl implements MailSender {

  private final static Logger LOGGER = LoggerFactory.getLogger(MailSenderImpl.class);

  private final JavaMailSender javaMailSender;
  private final Configuration freemarkerConfiguration;
  private final MessageSource messageSource;
  private final String basePath;
  private final Locale defaultLocale;
  private final MailFilter filter;
  private final AsyncMailSender asyncMailSender;
  private final InternetAddress from;
  private final Set<Pattern> filters;
  private final MailjetClient mailjetClient;
  private final boolean mailjetEnabled;

  public MailSenderImpl(JavaMailSender javaMailSender, Configuration freemarkerConfiguration,
                        MessageSource messageSource, String basePath, Locale defaultLocale,
                        MailFilter filter, AsyncMailSender asyncMailSender, InternetAddress from,
                        Set<String> filters, MailjetClient mailjetClient,boolean mailjetEnabled) {
      this.mailjetClient = mailjetClient;
      this.mailjetEnabled = mailjetEnabled;
      Preconditions.checkNotNull(javaMailSender);
    Preconditions.checkNotNull(freemarkerConfiguration);
    Preconditions.checkNotNull(messageSource);
    Preconditions.checkNotNull(basePath);
    Preconditions.checkNotNull(defaultLocale);
    Preconditions.checkNotNull(filter);
    this.javaMailSender = javaMailSender;
    this.freemarkerConfiguration = freemarkerConfiguration;
    this.messageSource = messageSource;
    this.basePath = basePath;
    this.defaultLocale = defaultLocale;
    this.filter = filter;
    this.asyncMailSender = asyncMailSender;
    this.from = from;
    this.filters = filters.stream().map(Pattern::compile).collect(Collectors.toSet());
  }

  private class BlossomMailBuilder extends BlossomMailBuilderImpl {
    private final MailSenderImpl mailSender;

    BlossomMailBuilder(MailSenderImpl mailSender) {
      this.mailSender = mailSender;
    }

    @Override
    public BlossomMail build() {
      return new BlossomMail(textTemplate, textBody, htmlTemplate, htmlBody, mailSubject, locale, from, replyTo,
        highPriority, ctx, attachments, filter(to), filter(cc), filter(bcc), mailSender);
    }
  }

  private class BlossomMail extends BlossomMailImpl {
    private final MailSenderImpl mailSender;

    BlossomMail(String textTemplate, String textBody, String htmlTemplate, String htmlBody, String mailSubject,
                Locale locale, InternetAddress from, InternetAddress replyTo, Boolean highPriority,
                Map<String, Object> ctx, List<BlossomMailAttachment> attachments,
                Set<InternetAddress> to, Set<InternetAddress> cc, Set<InternetAddress> bcc, MailSenderImpl mailSender) {
      super(textTemplate, textBody, htmlTemplate, htmlBody, mailSubject, locale, from, replyTo, highPriority, ctx, attachments, to, cc, bcc);
      this.mailSender = mailSender;
    }

    @Override
    public void send() throws Exception {
      mailSender.send(this);
    }

    @Override
    public ListenableFuture<com.blossomproject.core.common.utils.mail.BlossomMail> asyncSend() {
      return mailSender.sendAsync(this);
    }
  }

  private ListenableFuture<com.blossomproject.core.common.utils.mail.BlossomMail> sendAsync(com.blossomproject.core.common.utils.mail.BlossomMail mail) {
    return asyncMailSender.asyncSend(mail);
  }

  @Override
  public com.blossomproject.core.common.utils.mail.BlossomMailBuilder builder() {
    BlossomMailBuilder ret = new BlossomMailBuilder(this);
    ret.locale(defaultLocale);
    ret.from(from);
    if (filters != null) {
      for (Pattern filter : filters) {
        ret.addFilter(filter);
      }
    }
    return ret;
  }

  private void send(BlossomMailImpl mail) throws Exception {
    Preconditions.checkArgument(mail.getLocale() != null);
    Preconditions.checkArgument(mail.getCtx() != null);
    Preconditions.checkArgument(mail.getTo() != null && !mail.getTo().isEmpty() || mail.getBcc() != null && !mail.getBcc().isEmpty());
    Preconditions.checkArgument(mail.getMailSubject() != null);
    Preconditions.checkArgument(Stream.of(mail.getHtmlTemplate(), mail.getHtmlBody(), mail.getTextBody(), mail.getTextTemplate()).anyMatch(Objects::nonNull));

    if(mailjetEnabled && mailjetClient != null){
      sendViaMailjet(mail);
      return;
    }
    final Map<String, Object> ctx = new HashMap<>(mail.getCtx());
    this.enrichContext(ctx, mail.getLocale());

    final MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
    final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");

    String htmlContent = null;
    if (mail.getHtmlTemplate() != null) {
      final Template template = this.freemarkerConfiguration.getTemplate("mail/" + mail.getHtmlTemplate() + ".ftl");
      htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, ctx);
    } else if (mail.getHtmlBody() != null) {
      htmlContent = mail.getHtmlBody();
    }

    String textContent = null;
    if (mail.getTextTemplate() != null) {
      final Template template = this.freemarkerConfiguration.getTemplate("mail/" + mail.getTextTemplate() + ".ftl");
      textContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, ctx);
    } else if (mail.getTextBody() != null) {
      textContent = mail.getTextBody();
    }

    if (htmlContent != null && textContent != null) {
      message.setText(textContent, htmlContent);
    } else if (htmlContent != null) {
      message.setText(htmlContent, true);
    } else if (textContent != null) {
      message.setText(textContent, false);
    }

    message.setFrom(mail.getFrom());
    if (mail.getReplyTo() != null) {
      message.setReplyTo(mail.getReplyTo());
    }

    final String subject = this.messageSource
      .getMessage(mail.getMailSubject(), new Object[]{}, mail.getMailSubject(), mail.getLocale());

    message.setSubject(subject);

    if (mail.getTo() != null) {
      message.setTo(mail.getTo().toArray(new InternetAddress[]{}));
    }

    if (mail.getCc() != null) {
      message.setCc(mail.getCc().toArray(new InternetAddress[]{}));
    }

    if (mail.getBcc() != null) {
      message.setBcc(mail.getBcc().toArray(new InternetAddress[]{}));
    }

    if (mail.getHighPriority()) {
      //https://www.chilkatsoft.com/p/p_471.asp
      mimeMessage.addHeader("X-Priority", "1");
      mimeMessage.addHeader("X-MSMail-Priority", "High");
      mimeMessage.addHeader("Importance", "High");
    }

    if (mail.getAttachments() != null) {
      for (BlossomMailAttachment attachment : mail.getAttachments()) {
        attachment.appendTo(message);
      }
    }

    try {
      this.javaMailSender.send(this.filter.filter(message));
    } catch (Exception e) {
      LOGGER.error("Error when sending", e);
    }

    LOGGER.info("Mail with recipient(s) {To: '{}', CC: '{}', BCC: '{}'} sent.",
      Arrays.toString(message.getMimeMessage().getRecipients(Message.RecipientType.TO)),
      Arrays.toString(message.getMimeMessage().getRecipients(Message.RecipientType.CC)),
      Arrays.toString(message.getMimeMessage().getRecipients(Message.RecipientType.BCC)));
  }

  private void sendViaMailjet(BlossomMailImpl mail) throws Exception {

    final Map<String, Object> ctx = new HashMap<>(mail.getCtx());
    this.enrichContext(ctx, mail.getLocale());


    String htmlContent = null;
    if (mail.getHtmlTemplate() != null) {
      final Template template = this.freemarkerConfiguration.getTemplate("mail/" + mail.getHtmlTemplate() + ".ftl");
      htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, ctx);
    } else if (mail.getHtmlBody() != null) {
      htmlContent = mail.getHtmlBody();
    }

    String textContent = null;
    if (mail.getTextTemplate() != null) {
      final Template template = this.freemarkerConfiguration.getTemplate("mail/" + mail.getTextTemplate() + ".ftl");
      textContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, ctx);
    } else if (mail.getTextBody() != null) {
      textContent = mail.getTextBody();
    }

    TransactionalEmail.TransactionalEmailBuilder emailbuilder = TransactionalEmail.builder();
    Stream.concat(mail.getBcc().stream(),Stream.concat(mail.getCc().stream(),mail.getTo().stream())).forEach(dest -> {
      emailbuilder.to(new SendContact(dest.getAddress()));
    });

    emailbuilder.from(new SendContact(mail.getFrom().getAddress())).htmlPart(htmlContent).textPart(textContent).attachments(convertAttachment(mail.getAttachments())).subject(mail.getMailSubject());
    var emailRequest = SendEmailsRequest.builder().message(emailbuilder.build()).build();
   try{
     var response = emailRequest.sendWith(mailjetClient );
     LOGGER.info("Result from email sending via mailjet : {}", response);
   }catch (Exception e){
     LOGGER.error("Unable to send mailjet email : {}", e.getMessage());
   }
  }

  private List<Attachment> convertAttachment(List<BlossomMailAttachment> attachments){
    return attachments.stream().map(this::convertAttachment).filter(Objects::nonNull).collect(Collectors.toList());
  }

  Attachment convertAttachment(BlossomMailAttachment attachment){
    if(attachment instanceof InputStreamMailAttachment inputStreamMailAttachment){

      return Attachment.builder().filename(inputStreamMailAttachment.getFilename()).contentType(inputStreamMailAttachment.getContentType()).base64Content(convertInputStreamToString(inputStreamMailAttachment.getSource())).build();
    }else if(attachment instanceof FileMailAttachment fileMailAttachment){
    return Attachment.builder().build();
    }
return null;
  }


  private String convertInputStreamToString(InputStreamSource inputStreamSource){
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[8192]; // 8KB buffer
    int bytesRead;
    try{
      var inputStream = inputStreamSource.getInputStream();
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
    }catch (Exception e){
      LOGGER.error("Unable to convert to base64 string : {}", e.getMessage());
    }

    byte[] bytes = outputStream.toByteArray();
    return Base64.getEncoder().encodeToString(bytes);
  }

  private void enrichContext(Map<String, Object> ctx, Locale locale) {
    ctx.put("basePath", this.basePath);
    ctx.put("message", new MessageResolverMethod(this.messageSource, locale));
    ctx.put("lang", locale);
  }

  private class MessageResolverMethod implements TemplateMethodModelEx {

    private MessageSource messageSource;
    private Locale locale;

    public MessageResolverMethod(MessageSource messageSource, Locale locale) {
      this.messageSource = messageSource;
      this.locale = locale;
    }

    @Override
    public Object exec(List arguments) throws TemplateModelException {
      if (arguments.size() != 1) {
        throw new TemplateModelException("Wrong number of arguments");
      }
      String code = ((SimpleScalar) arguments.get(0)).getAsString();
      if (code == null || code.isEmpty()) {
        throw new TemplateModelException("Invalid code value '" + code + "'");
      }
      return messageSource.getMessage(code, null, locale);
    }
  }

}
