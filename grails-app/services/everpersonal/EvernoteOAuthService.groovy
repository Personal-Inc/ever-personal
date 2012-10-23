package everpersonal

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

import org.scribe.builder.ServiceBuilder
import org.scribe.model.Token
import org.scribe.model.Verifier

import com.evernote.oauth.consumer.EvernoteApi
import com.evernote.oauth.consumer.EvernoteApi.EvernoteSandboxApi

class EvernoteOAuthService {

    static transactional = true

    /**
     * @return userAuthURL, requestTokenSecret
     */
    def getUserAuthParams() {
        def oAuth = buildOAuthService()
        def requestToken = oAuth.requestToken
        def response = [:]
        response.userAuthURL = oAuth.getAuthorizationUrl(requestToken)
        response.requestTokenSecret = requestToken.secret
        response
    }

    /**
     * @param requestTokenSecret, oauth_token, oauth_verifier
     * @return accessToken, noteStoreUrl
     */
    def handleUserAuthResponse(params) {
        def oAuth = buildOAuthService()
        def requestTokenVal = params.oauth_token
        def requestTokenSecretVal = params.requestTokenSecret
        def verifierVal = params.oauth_verifier
        if (!verifierVal)
            return null // user declined

        def requestToken = new Token(requestTokenVal, requestTokenSecretVal)
        def verifier = new Verifier(verifierVal)
        def accessToken = oAuth.getAccessToken(requestToken, verifier)

        def response = [:]
        response.accessToken = accessToken.token
        response.noteStoreUrl = accessToken.noteStoreUrl
        response.userId = accessToken.userId
        response
    }

    private def buildOAuthService() {
        def apiClass = CH.config.evernote.sandbox ? EvernoteSandboxApi : EvernoteApi
        new ServiceBuilder()
          .provider(apiClass)
          .apiKey(CH.config.evernote.consumerKey)
          .apiSecret(CH.config.evernote.consumerSecret)
          .callback("${CH.config.grails.serverURL}/evernote/oauth1callback")
          .build()
    }
}
