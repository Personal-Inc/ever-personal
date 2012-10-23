package everpersonal

import groovyx.net.http.ContentType

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

class PersonalOAuthService {
    // ref: http://developer.personalapi.com/docs/read/authentication/Authorization_Code

    static transactional = true

    def buildURLForUserAuth() {
        "${CH.config.personalapi.host}/oauth/authorize?client_id=${CH.config.personalapi.key}" +
        "&response_type=code&redirect_uri=${CH.config.grails.serverURL}/personal/oauth2callback" +
        "&scope=create_${CH.config.personalapi.gemTemplateId}"
    }

    // returns tokens: access_token, refresh_token, expires_in
    def handleUserAuthResponse(code) {
        def tokens = [:]
        // exchange code for access token
        withRest(uri: "${CH.config.personalapi.host}/oauth/access_token") {
            def body = "grant_type=authorization_code&code=${code}" +
                "&client_id=${CH.config.personalapi.key}" +
                "&client_secret=${CH.config.personalapi.sharedSecret}" +
                "&redirect_uri=${CH.config.grails.serverURL}/personal/oauth2callback"
            def response = post(body: body, requestContentType: ContentType.URLENC, contentType: ContentType.JSON)
            def json = response.data
            tokens.access_token = json.access_token
            tokens.refresh_token = json.refresh_token
            tokens.expires_in = json.expires_in as long
        }
        tokens
    }

    // returns tokens: access_token, refresh_token, expires_in
    def refreshTokens(access_token, refresh_token) {
        def tokens = [:]
        withRest(uri: "${CH.config.personalapi.host}/oauth/access_token") {
            def body = "grant_type=refresh_token&refresh_token=${refresh_token}" +
                "&client_id=${CH.config.personalapi.key}" +
                "&client_secret=${CH.config.personalapi.sharedSecret}" +
                "&redirect_uri=${CH.config.grails.serverURL}/personal/oauth2callback"
            def response = post(body: body, requestContentType: ContentType.URLENC, contentType: ContentType.JSON)
            def json = response.data
            tokens.access_token = json.access_token
            tokens.refresh_token = json.refresh_token
            tokens.expires_in = json.expires_in
        }
        log.info "refreshed tokens"
        tokens
    }
}
