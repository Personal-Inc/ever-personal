package everpersonal

import groovyx.net.http.ContentType

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import org.codehaus.jackson.map.ObjectMapper

class PersonalService {

    static final API_URL = "${CH.config.personalapi.host}/api/v1"
    static final GEMS_API_URL = "${API_URL}/gems"

    static transactional = true

    // returns gem_instance_id
    def createGem(access_token, title, note, noteUrl) {
        // ref: http://developer.personal.com/docs/read/gems/Creating_a_new_gem
        def body = new ObjectMapper().writeValueAsString(
            [gem: [
                info: [
                    gem_instance_name: title as String,
                    gem_template_id: CH.config.personalapi.gemTemplateId as String],
                data: [
                    note_title: title as String,
                    note_note: note as String,
                    note_note_url: noteUrl as String]]])

        def gem_instance_id
        withRest(uri: "${GEMS_API_URL}") {
            headers.Authorization = authHeader(access_token)
            headers.'Secure-Password' = CH.config.personalapi.securePassword

            def response = post(body: body, contentType: ContentType.JSON,
                    query: [client_id: CH.config.personalapi.key])
            def json = response.data
            gem_instance_id = json.gem.info.gem_instance_id
        }
        gem_instance_id
    }

    // returns gemData (updated_timestamp, title, note, noteUrl, attachments (name, url_file))
    def getGem(access_token, gem_instance_id) {
        // ref: http://developer.personal.com/docs/read/gems/Read_gem
        def gemData = [:]
        withRest(uri: "${GEMS_API_URL}/${gem_instance_id.encodeAsURL()}") {
            headers.Authorization = authHeader(access_token)
            headers.'Secure-Password' = CH.config.personalapi.securePassword

            def response = get(contentType: ContentType.JSON,
                    query: [client_id: CH.config.personalapi.key])
            def json = response.data
            gemData.updated_timestamp = json.gem.info.updated_timestamp as long
            gemData.title = json.gem.data.note_title
            gemData.note = json.gem.data.note_note
            gemData.noteUrl = json.gem.data.note_note_url
            gemData.attachments = []
            json.gem.attachments.each { attachmentId, attachmentData ->
                gemData.attachments << [name: attachmentData.name, url_file: attachmentData.url_file]
            }
        }
        gemData
    }

    def updateGem(access_token, gem_instance_id, gemData) {
        // ref: http://developer.personal.com/docs/read/gems/Updating_a_gem
        def body = new ObjectMapper().writeValueAsString(
            [gem: [
                info: [
                    gem_instance_id: gem_instance_id as String,
                    gem_instance_name: gemData.title as String,
                    gem_template_id: CH.config.personalapi.gemTemplateId as String,
                    updated_timestamp: gemData.updated_timestamp as long],
                data: [
                    note_title: gemData.title as String,
                    note_note: gemData.note as String,
                    note_note_url: gemData.noteUrl as String]]])

        withRest(uri: "${GEMS_API_URL}/${gem_instance_id.encodeAsURL()}") {
            headers.Authorization = authHeader(access_token)
            headers.'Secure-Password' = CH.config.personalapi.securePassword

            def response = put(body: body, contentType: ContentType.JSON,
                    query: [client_id: CH.config.personalapi.key])
        }
    }

    def uploadFileToGem(access_token, gem_instance_id, fileName, fileBytes) {
        // ref: http://developer.personal.com/docs/read/gems/attachments/Upload_Files
        withRest(uri: "${CH.config.personalapi.host}/file") {
            headers.Authorization = authHeader(access_token)
            headers.'Secure-Password' = CH.config.personalapi.securePassword

            def response = post(body: fileBytes, requestContentType: ContentType.BINARY,
                    contentType: ContentType.JSON,
                    query: [
                        'files[]': fileName,
                        gem_id: gem_instance_id,
                        client_id: CH.config.personalapi.key])
        }
    }

    def deleteFileFromGem(access_token, gem_instance_id, url_file) {
        // ref: http://developer.personal.com/docs/read/gems/attachments/Delete_Files_on_the_Personal_Flie_Service
        withRest(uri: url_file) {
            headers.Authorization = authHeader(access_token)
            headers.'Secure-Password' = CH.config.personalapi.securePassword

            def response = delete(query: [client_id: CH.config.personalapi.key])
        }
    }

    private def authHeader(access_token) {
        "Bearer ${access_token}"
    }
}
