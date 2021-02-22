# cgi-servlet-container

This is a minimal container to deploy a servlet using the CGI API.

Limitations:

* Only one servlet is supported. My motivation here is to deploy Javalin, and that does everything from a single servlet.
* Async servlet methods not supported - the CGI request model makes this kinda pointless.
* Session not supported. It would just be an ID in a cookie, mapped to a persistent store anyway. Maybe I'll come back
to this with an interface to plug a store in.
  
* Locale handling not implemented. Maybe I'll come back to that.
* Left out most of the auth related stuff. The servlet model for auth seems kinda out of date now. Like CGI :D