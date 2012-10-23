class UrlMappings {

	static mappings = {
        "/evernote/oauth1callback"(controller: 'register', action: 'handleEvernoteUserAuthResponse')
        "/personal/oauth2callback"(controller: 'register', action: 'handlePersonalUserAuthResponse')

        "/evernote/webhook"(controller: 'evernote', action: 'handleNotification')

		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

        "/"(controller: 'register')
		"500"(view:'/error')
	}
}
