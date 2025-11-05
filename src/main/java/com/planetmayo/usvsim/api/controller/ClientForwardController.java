package com.planetmayo.usvsim.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Forwards non-API routes to React index.html for client-side routing.
 *
 * React Router uses browser history API for navigation (/mission, /simulation, etc.)
 * Without this controller, direct URL access or refresh would return 404.
 *
 * Strategy:
 * - API routes (/api/**) handled by REST controllers
 * - Static resources (/*.js, /*.css, /*.png) served directly
 * - All other routes forwarded to /index.html for React Router
 */
@Controller
public class ClientForwardController {

    /**
     * Forward all non-API, non-static routes to React application for client-side routing.
     * Excludes: /api/**, /static/**, files with extensions (*.js, *.css, etc.)
     *
     * Note: Spring Boot 3.x path pattern syntax doesn't allow anything after **.
     * This uses negative lookahead to exclude api and static paths, plus a pattern
     * to exclude files with extensions.
     */
    @GetMapping(value = "/{path:(?!api|static)(?!.*\\.).*}")
    public String forward() {
        return "forward:/index.html";
    }
}
