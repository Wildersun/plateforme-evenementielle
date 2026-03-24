package web;

public class HtmlTemplate {
    
    // Header standard pour toutes les pages (inclus Bootstrap et FontAwesome si on veut, ou du CSS natif très premium)
    public static String getHeader(String title) {
        return "<!DOCTYPE html>\n" +
               "<html lang=\"fr\">\n" +
               "<head>\n" +
               "    <meta charset=\"UTF-8\">\n" +
               "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
               "    <title>" + title + " - Plateforme Événementielle</title>\n" +
               "    <style>\n" +
               "        :root {\n" +
               "            --primary: #4F46E5;\n" +
               "            --primary-hover: #4338CA;\n" +
               "            --secondary: #10B981;\n" +
               "            --secondary-hover: #059669;\n" +
               "            --dark: #111827;\n" +
               "            --light: #F3F4F6;\n" +
               "            --gray: #6B7280;\n" +
               "            --danger: #EF4444;\n" +
               "        }\n" +
               "        body {\n" +
               "            font-family: 'Inter', -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, sans-serif;\n" +
               "            background-color: var(--light);\n" +
               "            color: var(--dark);\n" +
               "            margin: 0;\n" +
               "            padding: 0;\n" +
               "            line-height: 1.6;\n" +
               "            display: flex;\n" +
               "            flex-direction: column;\n" +
               "            min-height: 100vh;\n" +
               "        }\n" +
               "        .navbar {\n" +
               "            background: white;\n" +
               "            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);\n" +
               "            padding: 1rem 2rem;\n" +
               "            display: flex;\n" +
               "            justify-content: space-between;\n" +
               "            align-items: center;\n" +
               "            position: sticky;\n" +
               "            top: 0;\n" +
               "            z-index: 1000;\n" +
               "        }\n" +
               "        .navbar-brand {\n" +
               "            font-size: 1.5rem;\n" +
               "            font-weight: 800;\n" +
               "            color: var(--primary);\n" +
               "            text-decoration: none;\n" +
               "            letter-spacing: -0.025em;\n" +
               "        }\n" +
               "        .nav-links {" +
               "            display: flex;\n" +
               "            gap: 1.5rem;\n" +
               "            align-items: center;\n" +
               "        }\n" +
               "        .nav-link {\n" +
               "            color: var(--gray);\n" +
               "            text-decoration: none;\n" +
               "            font-weight: 500;\n" +
               "            transition: color 0.2s;\n" +
               "        }\n" +
               "        .nav-link:hover { color: var(--primary); }\n" +
               "        .btn {\n" +
               "            display: inline-flex;\n" +
               "            align-items: center;\n" +
               "            justify-content: center;\n" +
               "            padding: 0.5rem 1.2rem;\n" +
               "            border-radius: 0.5rem;\n" +
               "            font-weight: 600;\n" +
               "            text-decoration: none;\n" +
               "            transition: all 0.2s;\n" +
               "            border: none;\n" +
               "            cursor: pointer;\n" +
               "        }\n" +
               "        .btn-primary {\n" +
               "            background-color: var(--primary);\n" +
               "            color: white;\n" +
               "            box-shadow: 0 4px 6px -1px rgba(79, 70, 229, 0.4);\n" +
               "        }\n" +
               "        .btn-primary:hover { background-color: var(--primary-hover); transform: translateY(-1px); }\n" +
               "        .btn-secondary {\n" +
               "            background-color: var(--secondary);\n" +
               "            color: white;\n" +
               "        }\n" +
               "        .btn-secondary:hover { background-color: var(--secondary-hover); transform: translateY(-1px); }\n" +
               "        .container {\n" +
               "            max-width: 1200px;\n" +
               "            margin: 0 auto;\n" +
               "            padding: 2rem;\n" +
               "            flex: 1;\n" +
               "            width: 100%;\n" +
               "            box-sizing: border-box;\n" +
               "        }\n" +
               "        .card {\n" +
               "            background: white;\n" +
               "            border-radius: 1rem;\n" +
               "            box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);\n" +
               "            overflow: hidden;\n" +
               "            transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.3s;\n" +
               "            display: flex;\n" +
               "            flex-direction: column;\n" +
               "        }\n" +
               "        .card:hover {\n" +
               "            transform: translateY(-5px);\n" +
               "            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);\n" +
               "        }\n" +
               "        .card-body {\n" +
               "            padding: 1.5rem;\n" +
               "            flex: 1;\n" +
               "            display: flex;\n" +
               "            flex-direction: column;\n" +
               "        }\n" +
               "        .card-title {\n" +
               "            font-size: 1.25rem;\n" +
               "            font-weight: 700;\n" +
               "            margin-top: 0;\n" +
               "            margin-bottom: 0.5rem;\n" +
               "            color: var(--dark);\n" +
               "        }\n" +
               "        .card-text {\n" +
               "            color: var(--gray);\n" +
               "            margin-bottom: 1rem;\n" +
               "        }\n" +
               "        .badge {\n" +
               "            display: inline-block;\n" +
               "            padding: 0.25rem 0.75rem;\n" +
               "            border-radius: 9999px;\n" +
               "            font-size: 0.875rem;\n" +
               "            font-weight: 600;\n" +
               "            background-color: #E0E7FF;\n" +
               "            color: var(--primary);\n" +
               "        }\n" +
               "        .form-group {\n" +
               "            margin-bottom: 1.5rem;\n" +
               "        }\n" +
               "        label {\n" +
               "            display: block;\n" +
               "            font-weight: 600;\n" +
               "            margin-bottom: 0.5rem;\n" +
               "            color: var(--dark);\n" +
               "        }\n" +
               "        input[type=\"text\"], input[type=\"email\"], input[type=\"password\"], input[type=\"date\"], select {\n" +
               "            width: 100%;\n" +
               "            padding: 0.75rem 1rem;\n" +
               "            border-radius: 0.5rem;\n" +
               "            border: 1px solid #D1D5DB;\n" +
               "            background-color: #F9FAFB;\n" +
               "            color: var(--dark);\n" +
               "            font-size: 1rem;\n" +
               "            transition: border-color 0.2s, box-shadow 0.2s;\n" +
               "            box-sizing: border-box;\n" +
               "        }\n" +
               "        input:focus, select:focus {\n" +
               "            outline: none;\n" +
               "            border-color: var(--primary);\n" +
               "            box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.2);\n" +
               "            background-color: white;\n" +
               "        }\n" +
               "        .alert {\n" +
               "            padding: 1rem;\n" +
               "            border-radius: 0.5rem;\n" +
               "            margin-bottom: 1.5rem;\n" +
               "            font-weight: 500;\n" +
               "        }\n" +
               "        .alert-error { background-color: #FEE2E2; color: #991B1B; border-left: 4px solid var(--danger); }\n" +
               "        .alert-success { background-color: #D1FAE5; color: #065F46; border-left: 4px solid var(--secondary); }\n" +
               "        .footer {\n" +
               "            background-color: white;\n" +
               "            border-top: 1px solid #E5E7EB;\n" +
               "            padding: 2rem;\n" +
               "            text-align: center;\n" +
               "            color: var(--gray);\n" +
               "            font-size: 0.875rem;\n" +
               "        }\n" +
               "        .auth-card {\n" +
               "            max-width: 450px;\n" +
               "            margin: 2rem auto;\n" +
               "            padding: 2.5rem;\n" +
               "        }\n" +
               "        .page-header {\n" +
               "            margin-bottom: 2rem;\n" +
               "            text-align: center;\n" +
               "        }\n" +
               "        .page-title {\n" +
               "            font-size: 2.25rem;\n" +
               "            font-weight: 800;\n" +
               "            color: var(--dark);\n" +
               "            margin-bottom: 0.5rem;\n" +
               "        }\n" +
               "        .page-subtitle {\n" +
               "            font-size: 1.125rem;\n" +
               "            color: var(--gray);\n" +
               "        }\n" +
               "    </style>\n" +
               "</head>\n" +
               "<body>\n";
    }

    public static String getNavbar(boolean isLoggedIn, String role, String nomUser) {
        StringBuilder nav = new StringBuilder();
        nav.append("<nav class=\"navbar\">\n")
           .append("    <a href=\"/\" class=\"navbar-brand\">✨ EventManager</a>\n")
           .append("    <div class=\"nav-links\">\n");
        
        if (isLoggedIn) {
            nav.append("        <span class=\"nav-link\">Bonjour, <strong>").append(nomUser).append("</strong></span>\n");
            if ("client".equals(role)) {
                nav.append("        <a href=\"/client\" class=\"nav-link\">Événements</a>\n");
            } else if ("organisateur".equals(role)) {
                nav.append("        <a href=\"/organisateur\" class=\"nav-link\">Tableau de Bord</a>\n");
            } else if ("gerant".equals(role)) {
                nav.append("        <a href=\"/gerant\" class=\"nav-link\">Tableau de Bord Global</a>\n");
            }
            nav.append("    <div class=\"nav-actions\">\n");
            nav.append("      <div class=\"user-menu\" style=\"position: relative; display: inline-block;\">\n");
            nav.append("        <span style=\"font-weight: 500; font-size: 1.05rem; margin-right: 20px;\">").append(nomUser).append("</span>\n");
            nav.append("        <a href=\"/profile\" class=\"btn btn-secondary\" style=\"margin-right: 10px;\">Mon Profil</a>\n");
            nav.append("        <a href=\"/logout\" class=\"btn btn-secondary\">Déconnexion</a>\n");
            nav.append("      </div>\n");
            nav.append("    </div>\n");
        } else {
            nav.append("        <a href=\"/login\" class=\"nav-link\">Se connecter</a>\n")
               .append("        <a href=\"/register\" class=\"btn btn-primary\">S'inscrire</a>\n");
        }
        
        nav.append("    </div>\n")
           .append("</nav>\n")
           .append("<div class=\"container\">\n");
        return nav.toString();
    }

    public static String getFooter() {
        return "</div>\n" + // fermeture du .container
               "<footer class=\"footer\">\n" +
               "    <p>&copy; 2026 Plateforme Événementielle. POO Avancée.</p>\n" +
               "</footer>\n" +
               "</body>\n" +
               "</html>";
    }
}
