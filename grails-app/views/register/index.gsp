<html>
    <head>
        <title>EverPersonal</title>
    </head>

    <link rel="shortcut icon"
        href="${resource(dir:'images',file:'green_heart.png')}"
        type="image/png" />

    <g:set var="evernoteUrl" value=
        "${org.codehaus.groovy.grails.commons.ConfigurationHolder.config
            .evernote.sandPersonal
            ? 'http://sandPersonal.evernote.com' : 'http://evernote.com'}">
    </g:set>

    <g:set var="personalUrl" value=
        "${org.codehaus.groovy.grails.commons.ConfigurationHolder.config.personalapp.url}">
    </g:set>

    <body>
        <center>
            <h1 style="color: gray;">
                EverPersonal <font style="color: green;">&hearts;</font>
            </h1>
            <br/>
            <font size="+2">
                <g:link action="register">Register to <i>export notes</i> from
                <i><b>Evernote</b></i> to <i><b>Personal</b></i></g:link>
            </font>
            <p>
            <g:if test="${params.registered}">
                <font size="+1" color="green"><b>Congratulations ${params.email}!</b></font>
            </p>
            <p>
                Now notes you tag with <i>Personal</i> will be exported from
                <a href="${evernoteUrl}" target="_blank">Evernote</a> to
                <a href="${personalUrl}" target="_blank">Personal</a>.
            </p>
            <p>
                <i>Enjoy!</i>
                <br/><br/><br/>
            </g:if>
            <g:elseif test="${params.declined}">
                We're sorry you decided not to use <b>EverPersonal</b>.<br/>
                Thank you for your interest.
            </g:elseif>
            <g:else>
                Only notes you tag with <i>Personal</i> after you register will
                be exported from
                <a href="${evernoteUrl}" target="_blank">Evernote</a> to
                <a href="${personalUrl}" target="_blank">Personal</a>.
            </p>
            <p>
            When you choose to Register you will be asked to authorize this app
            first on Evernote, then on Personal.
            <br/><br/><br/>
            <p>
                <g:link action="deregister" onclick=
                "return confirm('Are you sure you want to deregister from EverPersonal?');">
                    <b>De</b>register
                </g:link>
            </g:else>
            </p>
        </center>
    </body>
</html>
