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
     * Forward all non-API routes to React application.
     * Excludes: /api/**, static resources with extensions
     */
    @GetMapping(value = {
        "/",
        "/{path:[^\\.]*}",           // Any path without dot (no file extension)
        "/**/{path:[^\\.]*}"         // Nested paths without dot
    })
    public String forward() {
        return "forward:/index.html";
    }
}
