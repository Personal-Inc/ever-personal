package com.evernote.oauth.consumer;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;

/** A Scribe Api class for the Evernote web service API. */
public class EvernoteApi extends DefaultApi10a {
  protected String getEvernoteHost() {
      return "www.evernote.com";
  }

  @Override
  public String getAccessTokenEndpoint() {
    return "https://" + getEvernoteHost() + "/oauth";
  }

  @Override
  public String getAuthorizationUrl(Token requestToken) {
    String template = "https://" + getEvernoteHost() + "/OAuth.action?oauth_token=%s";
    return String.format(template, requestToken.getToken());
  }

  @Override
  public String getRequestTokenEndpoint() {
    return "https://" + getEvernoteHost() + "/oauth";
  }

  @Override
  public org.scribe.extractors.AccessTokenExtractor getAccessTokenExtractor() {
    return new AccessTokenExtractor();
  }

  /** A Scribe Api class for the Evernote sandbox API. */
  public static class EvernoteSandboxApi extends EvernoteApi {
    @Override
    protected String getEvernoteHost() {
        return "sandbox.evernote.com";
    }
  }
}