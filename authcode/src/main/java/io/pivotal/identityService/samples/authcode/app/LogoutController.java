package io.pivotal.identityService.samples.authcode.app;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class LogoutController {
    @PostMapping("/logout")
    public String authorizationCode(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = WebUtils.getCookie(request, "__Host-acme-id-token-session-cookie");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        // TODO shall we try calling /logout.do on UAA? We would need to read the hostname of UAA from the vcap var

        return "redirect:/";
    }
}
