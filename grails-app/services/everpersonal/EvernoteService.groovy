package everpersonal

import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.THttpClient
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

import com.evernote.edam.error.EDAMNotFoundException
import com.evernote.edam.notestore.NoteStore
import com.evernote.edam.type.Tag

class EvernoteService {

    static transactional = true

    def getNote(guid, accessToken, noteStoreUrl) {
        def withContent = false
        def withResourcesData = true
        def withResourcesRecognition = false
        def withResourcesAlternateData = false

        def noteStore = getNoteStore(noteStoreUrl)
        noteStore.getNote(accessToken, guid,
            withContent,
            withResourcesData,
            withResourcesRecognition,
            withResourcesAlternateData)
    }

    def getNoteContent(guid, accessToken, noteStoreUrl) {
        def noteOnly = true
        def tokenizeForIndexing = false
        def noteStore = getNoteStore(noteStoreUrl)
        noteStore.getNoteSearchText(accessToken, guid, noteOnly, tokenizeForIndexing)
    }

    def getLinkToNote(guid) {
        def server = CH.config.evernote.sandbox ?
            'https://sandbox.evernote.com' : 'https://www.evernote.com'
        "${server}/Home.action#n=${guid}"
    }

    def getOurApplicationDataFromNote(note, accessToken, noteStoreUrl) {
        // first look in our cache to avoid unnecessary call out to Evernote
        def cachedData = NoteDataCache.findByNoteGuid(note.guid)
        if (cachedData)
            return cachedData.noteData

        // our app data is stored under our consumer key
        if (!note.attributes.applicationData?.keysOnly?.contains(
                CH.config.evernote.consumerKey))
            return null

        def noteStore = getNoteStore(noteStoreUrl)
        try {
            return noteStore.getNoteApplicationDataEntry(accessToken, note.guid,
                CH.config.evernote.consumerKey)

        } catch (EDAMNotFoundException e) {
            return null
        }
    }

    def setOurApplicationDataForNote(userId, note, accessToken, noteStoreUrl, data) {
        // update our cache
        def cachedData = NoteDataCache.findByNoteGuid(note.guid)
        if (!cachedData)
            cachedData = new NoteDataCache(evernoteUserId: userId, noteGuid: note.guid)
        cachedData.noteData = data
        cachedData.save(failOnError: true)

        // then update Evernote
        def noteStore = getNoteStore(noteStoreUrl)
        noteStore.setNoteApplicationDataEntry(accessToken, note.guid,
            CH.config.evernote.consumerKey, data)
    }

    def createTag(tagName, accessToken, noteStoreUrl) {
        def tag = new Tag()
        tag.name = tagName as String

        def noteStore = getNoteStore(noteStoreUrl)
        try {
            noteStore.createTag(accessToken, tag)
        } catch (Exception e) {
            // tag already exists
        }
    }

    private def getNoteStore(noteStoreUrl) {
        def noteStoreTrans = new THttpClient(noteStoreUrl)
        def noteStoreProt = new TBinaryProtocol(noteStoreTrans)
        new NoteStore.Client(noteStoreProt, noteStoreProt)
    }
}
