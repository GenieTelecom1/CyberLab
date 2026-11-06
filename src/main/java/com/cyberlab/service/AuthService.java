package com.cyberlab.service;
import com.cyberlab.database.UserRepository;
import com.cyberlab.model.User;
import com.cyberlab.service.AuthException.Type;
import com.cyberlab.util.PasswordUtil;
import com.cyberlab.util.SessionManager;
import java.sql.SQLException;
import java.util.Optional;
public class AuthService {
private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z09.-]+[.][A-Za-z]{2,}$";
private static final String PG_UNIQUE_VIOLATION = "23505";
private final UserRepository repo = new UserRepository();
public User register(String nom, String email, String password, String 
confirm)
throws AuthException {
if (isBlank(nom) || isBlank(email) || isBlank(password) || 
isBlank(confirm)) {
throw new AuthException(Type.CHAMPS_VIDES, "Tous les champs sont obligatoires.");
        }
        String e = normalize(email);
        if (!e.matches(EMAIL_REGEX)) {
            throw new AuthException(Type.EMAIL_INVALIDE, "Adresse e-mail invalide.");
        }
        if (!isStrongEnough(password)) {
            throw new AuthException(Type.MOT_DE_PASSE_FAIBLE,
                    "Le mot de passe doit contenir au moins 8 caractères, une lettre et un chiffre.");
        }
        if (!password.equals(confirm)) {
            throw new AuthException(Type.MOTS_DE_PASSE_DIFFERENTS,
                    "Les mots de passe ne correspondent pas.");
        }
        try {
            User user = repo.insert(nom.trim(), e, PasswordUtil.hash(password));
            SessionManager.login(user);
            return user;
        } catch (SQLException ex) {
            if (PG_UNIQUE_VIOLATION.equals(ex.getSQLState())) {
                throw new AuthException(Type.EMAIL_DEJA_UTILISE, "Cet e-mail est déjà utilisé.");
            }
            throw technique(ex);
        }
    }
    public User login(String email, String password) throws AuthException {
        if (isBlank(email) || isBlank(password)) {
            throw new AuthException(Type.CHAMPS_VIDES, "Veuillez saisir votre e-mail et votre mot de passe.");
        }
        try {
            Optional<UserRepository.Credentials> found = 
repo.findCredentialsByEmail(normalize(email));
            if (found.isEmpty() || !PasswordUtil.verify(password, 
found.get().hash())) {
                throw new AuthException(Type.IDENTIFIANTS_INCORRECTS, "E-mail ou mot de passe incorrect.");
}
SessionManager.login(found.get().user());
return found.get().user();
} catch (SQLException ex) {
throw technique(ex);
}
}
public void logout() {
SessionManager.logout();
}
private static boolean isBlank(String s) { return s == null || 
s.isBlank(); }
private static String normalize(String email) { return 
email.trim().toLowerCase(); }
private static boolean isStrongEnough(String p) {
return p.length() >= 8
&& p.chars().anyMatch(Character::isLetter)
&& p.chars().anyMatch(Character::isDigit);
}
private static AuthException technique(SQLException ex) {
return new AuthException(Type.ERREUR_TECHNIQUE,
"Impossible de joindre la base de données. Vérifiez votre connexion Internet.", ex);
}
}