package everpersonal

import groovyx.net.http.HttpResponseException

class EvernoteController {

    def evernoteService
    def personalService
    def personalOAuthService

    // handler for Evernote webhook notification
    def handleNotification = {
        def userId = params.userId
        def guid = params.guid
        def reason = params.reason

        def reg = Registration.findByEvernoteUserId(userId)
        if (!reg) {
            def msg = "Received notification for unknown userId: [$userId]"
            log.error msg
            render(status: 404, text: msg)
            return
        }
        if (!reg.active) {
            def msg = "Received notification for inactive user: [$userId]"
            log.warn msg
            render(status: 404, text: msg)
            return
        }

        def note = evernoteService.getNote(guid, reg.evernoteAccessToken, reg.evernoteNoteStoreUrl)
        def link = evernoteService.getLinkToNote(guid)

        def title = note.title

        def content = evernoteService.getNoteContent(guid, reg.evernoteAccessToken, reg.evernoteNoteStoreUrl)
        // note content includes title and tags at end -> strip them out
        def titleAtEndOfContentIndex = content.lastIndexOf(title)
        if (titleAtEndOfContentIndex != -1) {
            content = content.substring(0, titleAtEndOfContentIndex)
        }
        content = content.trim().replace('  ', '\n') // 2 spaces represents a newline

        def gem_instance_id
        def gemAttachments = []

        if (reason == 'update') { // vs. 'create'
            gem_instance_id = evernoteService.getOurApplicationDataFromNote(
                note, reg.evernoteAccessToken, reg.evernoteNoteStoreUrl)
        }

        refreshPersonalTokensIfExpired(reg)

        if (!gem_instance_id) { // is new gem for Personal - need to create
            gem_instance_id = personalService.createGem(reg.personalAccessToken, title, content, link)

            evernoteService.setOurApplicationDataForNote(reg.evernoteUserId,
                note, reg.evernoteAccessToken, reg.evernoteNoteStoreUrl, gem_instance_id)

        } else { // existing gem - update it
            try {
                // first make sure we have the current gem data
                def gemData = personalService.getGem(reg.personalAccessToken, gem_instance_id)

                // then update it with any new data
                gemData.title = title
                gemData.note = content
                gemData.noteUrl = link
                personalService.updateGem(reg.personalAccessToken, gem_instance_id, gemData)

                // get list of attachment file names for step below
                gemData.attachments.each { attachment ->
                    gemAttachments << attachment.name
                }

            } catch (Exception e) {
                if (e.cause instanceof HttpResponseException) {
                    if (e.cause.response.status == 404) {
                        // gem was deleted, treat this as a 'create'
                        redirect(action: 'handleNotification', params:
                            [userId: params.userId, guid: params.guid, reason: 'create'])
                        return
                    }
                }
                throw e
            }
        }

        // add any NEW resources to gem as files
        note.resources.each { resource ->
            def fileName = resource.attributes.fileName
            def fileBytes = resource.data.body

            if (!gemAttachments.contains(fileName))
                personalService.uploadFileToGem(reg.personalAccessToken, gem_instance_id, fileName, fileBytes)
        }

        log.debug "handled Evernote [${reason}] notification for user [${userId}], note [${guid}]"

        render(status: 200, text: 'Success') // actually Evernote ignores response
    }

    private def refreshPersonalTokensIfExpired(reg) {
        if (new Date().after(reg.personalTokenExpiryDate)) {
            def tokens = personalOAuthService.refreshTokens(reg.personalAccessToken, reg.personalRefreshToken)
            reg.personalAccessToken = tokens.access_token
            reg.personalRefreshToken = tokens.refresh_token
            reg.personalTokenExpiryDate = new Date(new Date().time + tokens.expires_in*1000/*secs to millisecs*/)
            reg.save(failOnError: true)
        }
    }

}
