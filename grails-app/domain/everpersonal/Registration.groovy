package everpersonal

import java.util.Date;

class Registration {

    String evernoteUserId
    String evernoteNoteStoreUrl
    String evernoteAccessToken

    String personalAccessToken
    String personalRefreshToken
    Date personalTokenExpiryDate

    boolean active = true

    Date dateCreated
    Date lastUpdated

    static constraints = {
        evernoteUserId(unique: true)
        evernoteNoteStoreUrl()
        evernoteAccessToken()
        personalAccessToken()
        personalRefreshToken()
        personalTokenExpiryDate()
        active()
        dateCreated()
        lastUpdated()
    }

    static mapping = {
        evernoteUserId index: 'Evernote_UserId_Idx'
        cache true
    }
}
