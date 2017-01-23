# README #

This project aims at implementing a **WebApp to SIP gateway** in order to
access SIP services from any web browser. It has been issued in 2009 and is based on Google Web Toolkit framework for the client part. 

**NB**: this project is not maintained anymore since it is based on a old-fashioned AJAX polling approach.***It should be rewritten to use WebRTC current technologies***. However, most of the logic would be re-usable in WebRTC context.

## Content ##

### SIP Features ###

* Chatrooms / Instant Messaging
* Presence
* Telephony
* Multimedia Content Sharing

### Technologies ###

* HTML5 / AJAX application
* SIP / HTTP servlets on top of Tomcat-mobicents application server
(SIP API 1.1 – JSR 289)
* Java Applet based on Java Media Framework (JMF) for media
services (telephony, video)

### Architecture Overview ###
![Architecture](https://github.com/bbouffaut/ajax-to-sip-gateway/blob/master/Docs/ajax-2-sip-gateway-presentation.png)

## Set-up ##

### Prerequisites ###

* MySQL Server
* Tomcat-Mobicent
* GWT environment
* Running SIP Server for interconnection with eco-system

### Installation Guide ###

There is no detailed set-up guide. Please contact me if you need further details about project set-up.
