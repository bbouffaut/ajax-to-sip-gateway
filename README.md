# README #

This project aims at implementing a WebApp to SIP gateway in order to
access SIP services from any web browser. It has been issued in 2009 and is based on Google Web Toolkit framework for the client part. 

NB: this project is not maintained anymore since it is based on a AJAX polling approach. It should be rewritten to use WebRTC current technologies.

### Content ###

* SIP Features

– Chatrooms / Instant Messaging<br>
– Presence<br>
– Telephony<br>
– Multimedia Content Sharing<br>

* Technologies

- HTML5 / AJAX application<br>
- SIP / HTTP servlets on top of Tomcat-mobicents application server
(SIP API 1.1 – JSR 289)<br>
- Java Applet based on Java Media Framework (JMF) for media
services (telephony, video)<br>

* Architecture overview
![Chatroom Application Server presentation.png](https://bitbucket.org/repo/7jk8ry/images/3858223146-Chatroom%20Application%20Server%20presentation.png)

### How do I get set up? ###

This project requires:
- MySQL Server
- Tomcat-Mobicent
- GWT environment

There is no detailed set-up guide. Please contact me if you need further details about set-up of this project