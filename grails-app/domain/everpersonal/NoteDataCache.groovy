package everpersonal

class NoteDataCache {

    String evernoteUserId
    String noteGuid
    String noteData

    static constraints = {
        noteGuid(unique: true)
    }

    static mapping = {
        evernoteUserId index: 'Evernote_UserId_Idx'
        noteGuid index: 'NoteGuid_Idx'
        cache true
    }
}
