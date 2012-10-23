package everpersonal

class RegisterController {

    static final PERSONAL_EVERNOTE_TAG = 'Personal' // we will create this tag in Evernote

    def evernoteOAuthService
    def personalOAuthService
    def personalService
    def evernoteService

    def index = { }

    // handler for the user selecting to register
    def register = {
        // call our EvernoteOAuthService to get the userAuthParams, which
        //  consist of: the requestTokenSecret and the Evernote userAuthURL
        def userAuthParams = evernoteOAuthService.userAuthParams
        // store the requestTokenSecret in the session for use later
        session.requestTokenSecret = userAuthParams.requestTokenSecret
        // then redirect the user to the Evernote userAuthURL
        redirect(url: userAuthParams.userAuthURL)
    }

    // handler for the user selecting to DEregister
    def deregister = {
        // same as register handler except we set deregister in the session
        def userAuthParams = evernoteOAuthService.userAuthParams
        session.requestTokenSecret = userAuthParams.requestTokenSecret
        session.deregister = true
        redirect(url: userAuthParams.userAuthURL)
    }

    // handler for the user auth (OAuth) response from Evernote
    def handleEvernoteUserAuthResponse = {
        // now we have the oauth_token and oauth_verifier as params from Evernote
        // add the requestTokenSecret from the session to the params to pass
        //  to our EvernoteOAuthService to get the actual accessParams, which
        //  consist of: the accessToken, the noteStoreUrl, and the userId
        params.requestTokenSecret = session.requestTokenSecret
        def userDeclined = false
        def accessParams = evernoteOAuthService.handleUserAuthResponse(params)
        if (accessParams) {
            // save access params in session for use later and redirect to Personal auth
            session.evernoteAccessToken = accessParams.accessToken
            session.evernoteNoteStoreUrl = accessParams.noteStoreUrl
            session.evernoteUserId = accessParams.userId

            if (!session.deregister)
                redirect(action: 'personalAuthorization')
            else { // user deregistering
                inactivateRegistration()
                userDeclined = true
            }
        } else
            userDeclined = true

        session.deregister = null

        if (userDeclined) {
            // redirect to index page, expressing regret
            redirect(action: 'index', params: [declined: true])
        }
    }

    // action to authorize user on Personal, after authorizing them on Evernote
    def personalAuthorization = {
        def userAuthURL = personalOAuthService.buildURLForUserAuth()
        redirect(url: userAuthURL)
        // after user authorizes, Personal will redirect to our handler below
    }

    // handler for the user auth (OAuth) response from Personal
    // (won't get here if user declines/cancels in Personal auth screen)
    def handlePersonalUserAuthResponse = {
        def tokens = personalOAuthService.handleUserAuthResponse(params.code)

        // now save new Registration, overwriting old one if exists
        def reg = Registration.findByEvernoteUserId(session.evernoteUserId)
        if (!reg)
            reg = new Registration()
        reg.evernoteUserId = session.evernoteUserId
        reg.evernoteNoteStoreUrl = session.evernoteNoteStoreUrl
        reg.evernoteAccessToken = session.evernoteAccessToken
        reg.personalAccessToken = tokens.access_token
        reg.personalRefreshToken = tokens.refresh_token
        reg.personalTokenExpiryDate = new Date(new Date().time + tokens.expires_in*1000/*secs to millisecs*/)
        reg.active = true // reset if was false before
        reg.save(failOnError: true)

        evernoteService.createTag(PERSONAL_EVERNOTE_TAG, session.evernoteAccessToken, session.evernoteNoteStoreUrl)

        redirect(action: 'index', params: [registered: true])
    }

    private def inactivateRegistration() {
        def reg = Registration.findByEvernoteUserId(session.evernoteUserId)
        if (reg) {
            reg.active = false
            reg.save(failOnError: true)
        }
    }
}
