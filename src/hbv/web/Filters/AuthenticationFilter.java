package hbv.web.Filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Set;

@WebFilter("/*")
public class AuthenticationFilter implements Filter {

    private static final Set<String> ADMIN_PATHS = Set.of(
            "/Admin_dashboard.html",
            "/impfzentren.html",
            "/impfung.html",
            "/Impfzentrum_impfung.html",
            "/Timeslot.html",

            "/Centers",
            "/Vaccines",
            "/Vaccineslist",
            "/Impfzentrum_impfung",
            "/add-timeslot"
    );

    private static final Set<String> USER_PATHS = Set.of(
            "/buchen.html",
            "/listtermine.html",

            "/Buchen",
            "/appointment",
            "/DeleteAppointment",
            "/PDF",
            "/VerfügbareVaccineslist"
    
            
            );

    /*
     * Diese Endpunkte werden sowohl von Benutzern
     * als auch von Administratoren verwendet.
     */
    private static final Set<String> SHARED_PATHS = Set.of(
            "/Centerliste",
            "/Zeitslot",
            "/logout"
    );

    @Override
    public void doFilter(
            ServletRequest servletRequest,
            ServletResponse servletResponse,
            FilterChain filterChain
    ) throws IOException, ServletException {

        HttpServletRequest request =
                (HttpServletRequest) servletRequest;

        HttpServletResponse response =
                (HttpServletResponse) servletResponse;

        String path = request.getServletPath();

        if (path == null || path.isEmpty()) {
            path = "/";
        }

        boolean protectedPath =
                ADMIN_PATHS.contains(path)
                || USER_PATHS.contains(path)
                || SHARED_PATHS.contains(path);

        /*
         * Öffentliche Seiten und Dateien werden normal ausgeliefert.
         */
        if (!protectedPath) {
            filterChain.doFilter(request, response);
            return;
        }

        /*
         * Verhindert, dass geschützte Seiten nach dem Logout
         * über den Zurück-Button aus dem Browser-Cache erscheinen.
         */
        response.setHeader(
                "Cache-Control",
                "no-cache, no-store, must-revalidate"
        );
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        HttpSession session = request.getSession(false);

        boolean userLoggedIn =
                session != null
                && session.getAttribute("userId") != null;

        boolean adminLoggedIn =
                session != null
                && session.getAttribute("adminEmail") != null;

        if (ADMIN_PATHS.contains(path) && !adminLoggedIn) {
            rejectRequest(
                    request,
                    response,
                    "/admin_login.html"
            );
            return;
        }

        if (USER_PATHS.contains(path) && !userLoggedIn) {
            rejectRequest(
                    request,
                    response,
                    "/user_login.html"
            );
            return;
        }

        if (SHARED_PATHS.contains(path)
                && !userLoggedIn
                && !adminLoggedIn) {

            rejectRequest(
                    request,
                    response,
                    "/user_login.html"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void rejectRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            String loginPage
    ) throws IOException {

        String fetchMode = request.getHeader("Sec-Fetch-Mode");
        String accept = request.getHeader("Accept");

        boolean browserNavigation =
                "navigate".equalsIgnoreCase(fetchMode)
                || (accept != null && accept.contains("text/html"));

        if (browserNavigation) {
            response.sendRedirect(
                    request.getContextPath()
                    + loginPage
                    + "?error=session"
            );
            return;
        }

        /*
         * Antwort für XMLHttpRequest/AJAX.
         */
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"message\":\"Sie sind nicht eingeloggt.\"}"
        );
    }
}
