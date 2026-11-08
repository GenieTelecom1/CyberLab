# Mise en production : envoi d'e-mails et liste de contrôle

## 1. Activer l'envoi réel des codes

1. Copier `mail.properties.example` en `mail.properties` (racine du projet) et le remplir.
   Il est dans `.gitignore` : ne jamais le publier.
2. **Gmail** : activer la validation en 2 étapes, puis créer un « mot de passe d'application »
   (compte Google → Sécurité). Utiliser ce mot de passe (16 caractères), pas celui du compte.
   Hôte `smtp.gmail.com`, port `587`, sécurité `starttls`. L'expéditeur doit être l'adresse du compte.
3. Lancer l'application : le journal doit indiquer « Envoi d'e-mails par SMTP via … ».
   Sans configuration, l'application passe en mode développement (codes dans la console) et l'écrit
   dans le journal.
4. Variables d'environnement équivalentes (prioritaires) : `CYBERLAB_MAIL_HOST`, `_PORT`, `_USER`,
   `_PASSWORD`, `_FROM`, `_FROM_NAME`, `_SECURITY`.

## 2. Mode production

Définir `CYBERLAB_ENV=prod` (ou `-Dcyberlab.env=prod`). L'application refuse alors de démarrer sans
configuration d'e-mail, au lieu d'afficher les codes dans la console : un code lisible dans la console
permettrait à n'importe qui de valider une adresse qui n'est pas la sienne.

## 3. Liste de contrôle avant ouverture aux utilisateurs

- [ ] `mail.properties` et `db.properties` absents de Git et des livrables (`git ls-files | grep properties`).
- [ ] Mot de passe de la base réinitialisé (il a circulé entre plusieurs personnes) ; ancien invalide.
- [ ] `db/maintenance.sql` : plus aucun compte `@test.com` ; codes expirés purgés.
- [ ] RLS activé sur les 6 tables (Table Editor) ; test S4 de `J9_Rapport_tests.md` fait.
- [ ] Un vrai compte créé de bout en bout : e-mail reçu (vérifier aussi les courriers indésirables),
      code accepté, déconnexion, reconnexion.
- [ ] `CYBERLAB_ENV=prod` défini sur les machines des utilisateurs.
- [ ] Application finale (Membre 6) : appeler `AuthService.setEmailSender(EmailSenders.fromConfig())`
      et `DatabaseConnection.warmUp()` au début de `Application.start()`, `DatabaseConnection.close()`
      dans `stop()`.

## 4. Limites à connaître (non résolues par ce code)

- **Les identifiants sont embarqués dans l'application.** Le mot de passe de la base et le mot de passe
  SMTP doivent être présents sur chaque poste qui lance l'application. Pour un usage pédagogique ou une
  équipe de confiance, c'est acceptable. Pour une distribution publique, quiconque obtient l'application
  obtient aussi un accès direct à la base et la possibilité d'envoyer des e-mails en votre nom.
  La vraie solution est un petit serveur (API) qui détient ces secrets, ou Supabase Auth + un service
  d'e-mail hébergé. C'est un changement d'architecture, pas un réglage.
- **Limitation des essais côté application seulement** (`LoginAttemptLimiter`) : elle décourage mais ne
  protège pas contre quelqu'un qui contourne l'application.
- **Délivrabilité** : un compte Gmail personnel limite le volume (environ 500 e-mails par jour) et les
  messages peuvent finir en spam. Pour de vrais utilisateurs, utiliser un service d'envoi (Brevo, Mailjet,
  etc.) avec un domaine à vous (SPF/DKIM).
- **Pas de journal d'audit, pas de rôles, pas de sauvegarde automatique** de la base (voir J10, section 10).
